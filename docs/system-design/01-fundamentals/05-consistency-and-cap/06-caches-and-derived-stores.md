---
title: Caches and Derived Stores
---

# Caches and Derived Stores

## Overview
Caches and derived stores (materialized views, search indexes) accelerate reads but introduce additional consistency considerations. This page covers cache coherence patterns, staleness control, and how to keep derived data in sync using CDC and versioning.

## What, Why, When (and when‑not)
What
- Cache policies (cache‑aside, write‑through, write‑back, refresh‑ahead), invalidation strategies, and staleness budgets; CDC pipelines for derived stores (CQRS).

Why
- Reduce load and latency for hot paths; enable specialized queries (search, aggregations) without overloading OLTP stores.

When
- Hot read amplification; expensive computed views; global search or feed fan‑out.

When‑not
- Small datasets with sufficient primary DB performance; correctness‑critical reads that must be linearizable without extra complexity.

## Core concepts and variants
- Cache‑aside
  - Read miss → load from DB → populate cache; on write → update DB then invalidate cache.
- Write‑through
  - Writes go through cache to DB synchronously; cache always hot but higher write latency.
- Write‑back (write‑behind)
  - Buffer writes in cache then flush asynchronously; lowest write latency but complicates durability/ordering.
- Refresh‑ahead
  - Proactively refresh keys nearing TTL expiry to smooth tail latencies.
- Versioned keys
  - Embed version/commit_ts in the cache key to avoid stale reads without central invalidation races.
- Single‑flight/dogpile protection
  - Ensure only one loader populates a missing key; others wait to prevent thundering herd.

## Design decisions and trade‑offs
- TTL vs invalidation
  - Short TTL reduces staleness but increases miss rate; invalidation requires reliable signaling (e.g., after DB commit).
- Ordering and atomicity
  - DB write then cache invalidate can race with another reader populating stale data. Prefer write‑through or versioned keys to avoid races.
- Derived stores via CDC
  - Stream binlog/WAL to update search indexes/materialized views. Choose at‑least‑once with idempotent upserts; monitor lag and backfills.

## Algorithms and policies (conceptual)
Cache‑aside with versioned keys (≤ 25 lines)
```pseudo
function cacheKey(id, version):
  return "obj:"+id+":v:"+version

function readObj(id, minVersion=0):
  v = directory.lastKnownVersion(id)
  ver = max(v, minVersion)
  k = cacheKey(id, ver)
  x = cache.get(k)
  if x != MISS: return x
  # Single-flight
  with lock("load:"+id):
    x = cache.get(k)
    if x != MISS: return x
    rec = db.read(id)
    cache.set(cacheKey(id, rec.version), rec, ttl=TTL)
    return rec

function writeObj(id, changes):
  rec = db.update(id, changes)  # returns new version
  cache.set(cacheKey(id, rec.version), rec, ttl=TTL)  # write-through by version
  directory.updateVersion(id, rec.version)
  return rec
```

## Architecture and components
- Invalidation bus: publish (key, new_version) after DB commit; subscribers evict/mark stale.
- Directory of lastKnownVersion per key/tenant to compose cache keys and enforce RYW with minVersion fences.
- CDC pipeline to derived stores (search, analytics) with backfill tooling and idempotent upserts.

Mermaid: Cache‑aside with versioned keys
```mermaid
flowchart LR
  C[Client] --> G[Gateway]
  G -->|get v| D[(Dir: lastKnownVersion)]
  G -->|get obj:v| K[(Cache)]
  K -->|miss| DB[(Primary DB)]
  DB --> G
  G --> K: set obj:new_v
  DB --> Q[[CDC/Bus]]
  Q --> K: invalidate/advance v
  Q --> S[(Search/View)]
```

## Operational considerations
- Metrics: hit ratio, miss latency, dogpile rate, invalidation delay, stale read detections, CDC lag.
- Budgets: set TTL and staleness budgets per endpoint; track promotions/bypasses when RYW demands fresher data than cache provides.
- Runbooks: cache flush, warmup strategies, CDC backfill procedures with throttling and checkpoints.

## Examples

Example A (quantitative): TTL vs staleness
- If 95% of updates are read within 1 minute and your staleness budget is 30 seconds, set TTL ≤ 30s or use versioned keys + invalidation. Compute added read QPS from TTL misses and ensure capacity.

Example B (architectural): Product price updates
- On price change, DB commit returns version 101. Writer publishes (product_id, v=101). Cache uses key product:123:v:101. Readers request minVersion from session/dir; older cache entries are naturally bypassed.

## Edge cases and anti‑patterns
- Cache stampede on popular keys without single‑flight.
- Invalidate‑then‑write races causing stale reinsertion; prefer versioned keys or atomic multi‑write.
- Treating search index as source of truth; always reconcile from OLTP on conflicts.

## Interactions with adjacent topics
- [Session guarantees](./04-session-guarantees-and-client-techniques.md): use minVersion/min_commit_ts to preserve RYW.
- [Replication](../04-replication/): follower lag affects freshness; cache may need leader bypass on staleness exceedance.
- [Databases & Storage](../06-databases-and-storage/): MVCC version exposure for cache keys.

## Production checklist
- Choose policy (aside/through) per endpoint; define TTLs and invalidation rules.
- Implement single‑flight and versioned keys for hot items.
- Build CDC with idempotent upserts and backfill tooling; monitor lag and error budgets.

## Interview framing checklist
- Design cache coherence that preserves RYW and avoids dogpiles.
- Explain CDC‑based derived store updates and idempotency.
- Choose TTL/invalidation strategy given a staleness budget and access distribution.

## References
- DDIA (caching, materialized views); Google SRE (overload, caching)
- Redis/Memcached best practices; Debezium/CDC patterns
