---
title: Consistency, Coherence, Invalidation
---

# Consistency, coherence, and invalidation

This page turns the mental models from foundations into concrete guidance for keeping caches correct enough while staying fast. It focuses on:
- What “consistency” means for caches (vs databases)
- Client-centric guarantees like read‑your‑writes and monotonic reads
- Practical invalidation and refresh strategies across single and multi‑level caches
- Reliable fan‑out of invalidations in distributed systems

See also:
- Foundations and mental models: ./01-foundations.md
- Distributed caches in production: ./04-distributed-caches.md
- Multi‑level and hierarchical caching: ./06-multilevel-hierarchical.md
- Consistency models background: ../../05-consistency-and-cap/README.md

## What we mean by consistency and coherence in caches

- Consistency (cache ↔ origin): A cached value reflects the current value at the system of record (SoR). Caches rarely provide strict/strong consistency; we choose acceptable staleness windows instead.
- Coherence (cache ↔ cache): Multiple caches that store the same key agree with each other. Coherence breaks when one node updates/invalidates and others haven’t yet.
- Freshness: Age of a cached value. Policies like TTL, soft TTL, and revalidation bound freshness.

Client‑centric guarantees that matter to UX
- Read‑your‑writes (RYW): After a client writes, that same client’s subsequent reads see the effect. Achieved by write‑then‑invalidate (or write‑through) plus read routing that prefers fresh paths for that client.
- Monotonic reads: A single client never sees time go backwards (won’t read an older value after a newer one). Achieved by version checks/ETags, or by scoping reads to the same freshness domain.
- Causal consistency (often good enough): If A causes B, everyone eventually sees A before B. In practice: order invalidations and writes by a version/sequence and ignore out‑of‑order older updates.

## Core patterns: keep caches correct enough

1) TTL‑only (simple, baseline)
- Set an expiration (with jitter). Values may be stale until expiry. No coordination needed; easy but coarser freshness.

2) Cache‑aside with explicit invalidation (recommended default)
- Read: get → miss → fetch from origin → set with TTL → return.
- Write: write origin → invalidate cache key(s) immediately → optionally repopulate new value.
- Pros: Origin remains truth; minimal coupling. Cons: Races possible if invalidation is delayed; needs careful fan‑out in distributed systems.

3) Write‑through (update cache synchronously with origin)
- On write, update origin and cache in the same code path/transaction boundary if possible. Often still keep a TTL.
- Pros: Fewer immediate misses after writes. Cons: Coupling to cache on write path; still need fan‑out to other cache nodes/L1s.

4) Write‑behind (buffer writes in cache, persist later)
- Powerful for throughput but not a typical choice for “cache of record” scenarios; raises durability and ordering complexity. Prefer for derived/aggregated data only.

## Invalidation strategies and fan‑out

Single process
- Delete or update the key on write. Prefer idempotent deletes (safe to replay).

Many app instances or cache nodes
- Pub/Sub broadcast: Publish key names (or tags) to a channel; all instances subscribe and delete locally.
- Durable queue/outbox: For reliability, store invalidation events in an outbox table/stream and deliver at‑least‑once (e.g., with Debezium/Kafka). Handlers must be idempotent and version‑aware.
- Versioned keys: Encode a schema/version into keys (`entity:v2:<id>`). On incompatible changes, bump namespace to invalidate en masse without scanning.
- Tag/namespace invalidation: Maintain sets of keys per namespace/tag to bulk‑invalidate. Useful but non‑trivial in Redis/Memcached without extra bookkeeping.

Ordering, idempotency, and duplicates
- Assume duplicates and reordering. Include a version (logical clock) in payloads and ignore messages older than the last applied version.
- Prefer delete over “set to stale value”; deletes are idempotent and safe to replay.

Java — Redis pub/sub invalidation (simplified)
```java
import redis.clients.jedis.*;

// Publisher on write (delete first; publish idempotent invalidation)
try (Jedis j = new Jedis("localhost", 6379)) {
  String key = "user:v1:123";
  j.del(key);
  j.publish("invalidate", key);
}

// Subscriber process (each app instance keeps its L1/L2 coherent)
new Thread(() -> {
  try (Jedis j = new Jedis("localhost", 6379)) {
    j.subscribe(new JedisPubSub(){
      @Override public void onMessage(String ch, String msg){
        try (Jedis js = new Jedis("localhost", 6379)) {
          js.del(msg);
        }
      }
    }, "invalidate");
  }
}).start();
```

Python — Redis pub/sub invalidation
```python
import redis
r = redis.Redis(host='localhost', port=6379)

def on_write(user_id):
    key = f"user:v1:{user_id}"
    r.delete(key)               # idempotent
    r.publish('invalidate', key)

def subscriber():
    pubsub = r.pubsub()
    pubsub.subscribe('invalidate')
    for m in pubsub.listen():
        if m['type'] == 'message':
            r.delete(m['data'].decode())
```

Production note: Pub/Sub is best‑effort. If misses are unacceptable, pair with a durable outbox/stream and a replayer job to catch up missed invalidations.

## Achieving read‑your‑writes and monotonic reads

Pattern: write → invalidate → read bypass for the writing client
- After a successful write, mark a short‑lived client/session flag (e.g., 5–30s) that forces reads to bypass cache or to prefer the origin for that key.
- Alternative: store a per‑entity version (increment on write). Reads include version in the cache key (`user:v1:<id>:v42`). A read that observed v42 will never see v41 again (monotonic reads). Bump version on write, invalidate older keys lazily via TTL.

Sticky reads within a freshness domain
- For multi‑level caches (L1 in process, L2 Redis), read from the same L2 shard/replica for a session to reduce back‑and‑forth. Avoid reading from lagging replicas when strict RYW is required.

Compare‑and‑set and ETags
- Include an ETag/version in responses. On update, require If‑Match with the last seen ETag to prevent lost updates and to help clients reason about monotonicity.

## Preventing stampedes while staying fresh

- Soft TTL + stale‑while‑revalidate
  - Keep two TTLs: a “fresh” TTL and a longer “serve‑stale” TTL. If fresh TTL expired, serve stale for a short window while one request refreshes in background.
- Request coalescing (singleflight)
  - Ensure only one concurrent miss per key fetches from origin. Others wait or get stale.
- TTL jitter
  - Add ±5–10% jitter so many keys do not expire simultaneously.
- Serve stale on failure
  - If origin is down/slow, it’s often better to serve slightly stale data than to fail hard.

Pseudo‑code: cache‑aside with soft TTL and singleflight
```python
def get_with_swr(key, fetcher, fresh_ttl=300, stale_ttl=30):
    v = cache.get(key)
    if v and not v.is_expired():
        return v.data
    if v and v.is_expired() and not v.is_stale_expired(stale_ttl):
        # kick background refresh once
        if singleflight.acquire(key):
            try:
                data = fetcher()
                cache.set(key, data, ttl=fresh_ttl, jitter=True)
            finally:
                singleflight.release(key)
        return v.data  # serve stale
    # full miss
    data = fetcher()
    cache.set(key, data, ttl=fresh_ttl, jitter=True)
    return data
```

## Multi‑level invalidation (L1 + L2 + edge)

- L1 (in‑process) + L2 (Redis/Memcached)
  - Invalidate L2 on write and broadcast an event so each instance clears its L1 entry. Keep L1 TTL shorter than L2.
- CDN/Edge
  - Use cache‑busting via versioned URLs for immutable assets. For dynamic content with surrogate keys, purge by tag/group when your CDN supports it. For personalized content at the edge, prefer no‑store and cache safely behind the service boundary.

## Deploys and schema changes

- Versioned keys
  - Include a namespace version in keys (e.g., `user:v2:<id>`). On incompatible changes, bump the version to avoid partial reads of mixed formats.
- Shadow/warm caches
  - Pre‑warm the new namespace in the background; flip reads to the new namespace when hit ratio is healthy.

## Observability and correctness guardrails

- Metrics
  - Track hit/miss, evictions, invalidations/s, fan‑out latency, stale‑served count, request coalescing effectiveness, and error rates while refreshing.
- Alarms
  - Alert on rising stale‑served beyond budget, invalidation backlog, replication lag (for replica reads), and spikes in thundering‑herd behavior.
- Tracing
  - Annotate spans with cache events (hit/miss/stale/refresh/invalidate) to debug user‑visible freshness.

## Production checklist (consistency & invalidation)

- Keys
  - Namespaces with explicit versions; multitenancy encoded.
- Freshness policy
  - TTL + jitter decided per entity; soft TTL where appropriate; error‑mode serves stale.
- Write path
  - Origin write, then idempotent cache invalidation. If RYW needed, add session bypass or versioned keys.
- Fan‑out
  - Pub/Sub or durable outbox; handlers are idempotent and version‑aware.
- Multi‑level
  - L1 cleared via broadcast; L1 TTL shorter than L2. Edge/CDN purge patterns defined.
- Testing
  - Simulate delayed/duplicated invalidations; verify RYW and monotonic read properties with integration tests.


