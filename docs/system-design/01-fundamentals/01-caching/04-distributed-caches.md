---
title: Distributed Caches (Redis/Memcached)
---

# Distributed caches in production

This page is a practical guide for running distributed caches in production. It focuses on Redis and Memcached, when to use each, how to deploy them safely, and what to watch in operations. It builds on the foundations in the previous module and links to related topics for consistency and invalidation.

## When to choose a distributed cache
- You need a cache shared across many app instances or services.
- Data is read-heavy with acceptable staleness (see foundations for staleness budgets).
- You want controllable TTLs, eviction, and observability beyond in-process caches.
- Your working set doesn’t fit a single process but does fit a networked cache cluster.

See also: foundations for sizing and key design ideas in ./01-foundations.md and invalidation strategies in ./05-consistency-invalidation.md. For multi-level designs (local L1 + remote L2), see ./06-multilevel-hierarchical.md.

## Redis vs Memcached at a glance
- Redis
  - Rich data structures (strings, hashes, lists, sets, sorted sets, streams), atomic ops, Lua scripting.
  - Replication, sentinel-based HA, and native sharding via Redis Cluster.
  - Optional persistence (RDB/AOF) and durability trade-offs.
  - Great for counters, rate limits, leaderboards, small objects, ephemeral DB use-cases.
- Memcached
  - Very simple, in-memory only (no persistence), item TTLs, LRU eviction.
  - Scales via client-side consistent hashing; each node independent (no replication built-in).
  - Excellent for ephemeral caching when you want minimal features and maximum simplicity.

If you need rich primitives, HA with replicas, or persistence, pick Redis. If you want a very lean, blazing-fast, ephemeral cache with predictable memory use, Memcached is strong.

## Cluster topologies and sharding
- Redis Standalone + Sentinel
  - One primary with one or more replicas. Sentinel provides health checks, leader election, and failover.
  - Good for small deployments; simple mental model. Scale reads with replicas, writes still single-node.
- Redis Cluster (native sharding)
  - 16384 hash slots; each master owns a range. Key → CRC16 → slot. Clients auto-route and handle MOVED/ASK.
  - Use hash tags: key parts inside `{...}` determine the slot, letting you co-locate related keys for multi-key ops/pipelines.
  - Read replicas per master can scale reads; client must support READONLY for replica reads and accept eventual consistency.
- Memcached pools (client-side consistent hashing)
  - Clients pick server using consistent hashing (e.g., Ketama). No replication by default: a node loss → temporary misses until repopulated.
  - Middle-proxy options (e.g., Twemproxy) exist but add extra hop and failure domain.

Related: for general sharding principles, see ../../03-data-partitioning/README.md.

## Replication, reads, and failover behavior
- Redis replication
  - Async replication by default; a failover can lose last few writes. If loss is unacceptable, consider min-replicas-to-write (formerly min-slaves-to-write) to improve durability at write time.
  - Use replicas to offload reads. In Cluster, use READONLY clients to talk to replicas. Expect replica lag; avoid strongly consistent reads from replicas.
  - On failover, clients see MOVED/ASK or connection resets; production clients (Jedis, Lettuce, ioredis) will auto-discover and retry.
- Memcached
  - No native replication. Failover is client-driven: when a node is down, keys that hashed to it miss and repopulate on other nodes (depending on rehashing strategy). Prefer consistent hashing rings with limited movement.

## Durability and persistence (Redis)
- RDB snapshots
  - Periodic point-in-time snapshots to disk. Low overhead; at crash you lose changes since last snapshot.
- AOF (append-only file)
  - Logs every write; with appendfsync=everysec you risk ~1 second of data loss; appendfsync=always is safest but slower. AOF rewrite compacts growth periodically.
- Combined RDB + AOF rewrite is common: fast restarts plus bounded loss window.
- Memcached has no persistence. It’s intentionally ephemeral: on restart, all data is gone.

Rule of thumb: if your cache is strictly a performance layer and origin is source of truth, prioritize write performance and accept small loss on failover. If you use Redis for critical counters or business logic, calibrate AOF policy and replication accordingly.

## Memory management and eviction
- Redis
  - Set maxmemory and maxmemory-policy. Policies: noeviction, volatile-lru/lfu/ttl, allkeys-lru/lfu/random. LFU works well when access skew is high; LRU is a safe default.
  - LRU/LFU are approximations (sampled). Tune maxmemory-samples (e.g., 5–10) for better accuracy at a small CPU cost.
  - Fragmentation: Redis uses jemalloc; watch mem_fragmentation_ratio and RSS. Large, varied value sizes increase fragmentation. Consider compressing large values at the app layer and avoid megabyte-scale items.
  - Item sizing: prefer many small objects over few huge ones. Split large aggregates or store a compressed blob with a tight TTL.
- Memcached
  - Slab allocator with fixed-size classes. Very predictable but can cause growth/waste if value sizes vary widely. Tune slab automove/rebalancing.
  - Default max item is often 1 MB; increase with -I if truly needed, but consider splitting instead.

Cross-cutting guidance
- Keep average values small (sub‑10 KB typical). Very large items reduce effective capacity and increase fragmentation.
- Add jitter to TTLs (±5–10%) to avoid mass expiration events that stampede the origin.
- Use negative caching carefully (short TTL) to dampen repeated misses.

## Hot key and hot partition mitigation
- Spread load for hot keys
  - Duplicate hot content under a small set of derived keys (e.g., `key{0..N}`) and read from a random shard to fan out load. Write-through must update all duplicates.
  - In Redis Cluster, use different hash tags to ensure duplicates land on different masters.
- Read replicas and hybrid caches
  - Scale reads using replicas (Redis) and add an in-process L1 cache to absorb repeated reads per-instance. See ./06-multilevel-hierarchical.md.
- Request coalescing
  - Ensure only one miss per key triggers an origin fetch at a time (a “singleflight”/dedupe gate) to prevent dogpiles.
- Rate limit and backoff
  - If the cache is saturated (timeouts/queueing), fail fast and use fallbacks rather than cascading overload to origins.
- Detection
  - Monitor hot keys: redis-cli --hotkeys sampling; track top keys via keyspace hits/misses where feasible. In Memcached, use stats items/slabs and application telemetry.

## API patterns to use from applications
- Cache-aside (recommended default)
  - Read path: get; on miss fetch origin, set with TTL; return. Write path: write origin, then invalidate/update cache.
  - Easy to adopt and reason about. See invalidation strategies in ./05-consistency-invalidation.md.
- Read-through / write-through
  - Library or proxy performs cache interaction. Useful when centralizing policy. Risk: tight coupling and accidental load on cache.
- Write-behind
  - Buffer writes in cache then persist asynchronously. Powerful but increases failure-mode complexity.

For Redis, prefer atomic operations (INCR, HINCRBY, SETNX) or Lua scripts for read‑modify‑write sequences.

## Client configuration examples

Java (Jedis) — connection pool, timeouts, and pipelining
```java
import redis.clients.jedis.*;
import java.time.Duration;
import java.util.List;

JedisPoolConfig cfg = new JedisPoolConfig();
cfg.setMaxTotal(128);
cfg.setMaxIdle(32);
cfg.setMinIdle(8);
// Evict idle connections proactively
cfg.setTestWhileIdle(true);
cfg.setMinEvictableIdleTime(Duration.ofSeconds(30));
cfg.setTimeBetweenEvictionRuns(Duration.ofSeconds(15));

// Configure connect and read timeouts
try (JedisPool pool = new JedisPool(cfg, "localhost", 6379, 2000 /* connect ms */, 2000 /* soTimeout ms */);
     Jedis j = pool.getResource()) {
  // Batch with pipelines to reduce RTT
  Pipeline p = j.pipelined();
  for (int i = 0; i < 100; i++) {
    p.incr("counter:" + i);
  }
  List<Object> res = p.syncAndReturnAll();
}
```

Python (redis-py) — connection pool, timeouts, and pipeline
```python
import redis

pool = redis.ConnectionPool(
    host="localhost",
    port=6379,
    max_connections=128,
    socket_connect_timeout=2.0,
    socket_timeout=2.0,
    health_check_interval=30,
)
r = redis.Redis(connection_pool=pool)

pipe = r.pipeline()
for i in range(100):
    pipe.incr(f"counter:{i}")
results = pipe.execute()
```

Node.js (ioredis) — Cluster client with retries and READONLY for replicas
```js
const IORedis = require('ioredis');

const cluster = new IORedis.Cluster([
  { host: '127.0.0.1', port: 7000 },
  { host: '127.0.0.1', port: 7001 },
  { host: '127.0.0.1', port: 7002 }
], {
  scaleReads: 'replica', // read from replicas; expect eventual consistency
  redisOptions: {
    connectTimeout: 2000,
    maxRetriesPerRequest: 2,
    keepAlive: 1,
  }
});

async function bumpCounters() {
  const pipeline = cluster.pipeline();
  for (let i = 0; i < 100; i++) {
    pipeline.incr(`counter:${i}`);
  }
  const res = await pipeline.exec();
}

bumpCounters().catch(console.error);
```

## Operational guardrails and monitoring
- Timeouts and retry policy
  - Set bounded connect/read timeouts (1–3s typical). Use few retries with jitter and a strict deadline. Prefer fail fast over indefinite hang.
- Connection management
  - Use pools; avoid creating/destroying connections per request. Enable TCP keepalive to drop dead connections.
- Backpressure
  - If the cache is slow or saturated, cut off upstream calls quickly and serve fallbacks or stale data to protect origins.
- Observability (Redis)
  - Track: ops/sec, latency, hit/miss, evictions, used_memory vs maxmemory, mem_fragmentation_ratio, keyspace size, connected clients, replication lag, AOF/RDB timings, slowlog length and entries.
  - Tools: INFO, SLOWLOG GET, LATENCY DOCTOR, redis-cli --bigkeys/--hotkeys.
- Observability (Memcached)
  - Track: get/set rate, hit/miss, evictions, curr_items, bytes, slab distribution, evictions per slab, connection counts.
- Alarms
  - Alert on sustained high miss rate, rising evictions, high fragmentation, replication lag, slowlog spikes, and near‑OOME conditions.

## Failure modes and how to handle them
- Mass expiry and thundering herds
  - Add TTL jitter, use soft TTL + refresh-ahead, and implement request coalescing.
- Node loss in Memcached ring
  - Expect temporary elevated miss rate. Use consistent hashing to limit key movement. Consider warmup/preload for critical keys.
- Redis failover
  - Clients should reconnect and handle MOVED/ASK transparently. Accept that last writes may be lost with async replication.
- Data size drift and fragmentation
  - Watch memory metrics; compress or split large values; tune policies and sampling.
- Network partitions
  - Favor timeouts and retries with circuit breakers. For strict correctness needs, do not rely on replicas for reads.

## Security basics
- Use AUTH/ACLs and TLS on production deployments (or a private network perimeter) to prevent unauthorized access.
- Avoid embedding secrets in keys or values. Expire sensitive data promptly.

## Production checklist
- Keys
  - Namespaced and versioned keys; include multitenancy context if needed.
  - TTLs set with jitter; soft TTL for critical paths.
- Topology
  - Redis: Sentinel or Cluster chosen intentionally; replica counts decided; READONLY policy clear.
  - Memcached: consistent hashing ring, node failure playbook, item size limits reviewed.
- Persistence
  - Redis AOF/RDB policies set per risk tolerance; backup/restore tested.
- Memory
  - maxmemory and eviction policy defined; samples tuned; fragmentation monitored.
- Clients
  - Connection pooling, timeouts, retries with backoff; pipelining/batching used on hot paths.
- Observability
  - Dashboards for hit/miss, latency, ops/sec, evictions, memory, replication lag; slowlog monitored.
- Protection
  - Request coalescing and backpressure implemented; rate limits to origin defined.


