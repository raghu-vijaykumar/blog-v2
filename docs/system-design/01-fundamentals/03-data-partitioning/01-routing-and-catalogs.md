---
title: Routing and Catalogs
description: Deep dive into client libraries, gateways, and coordinators for shard routing; shard-map catalogs, versioning/TTLs, update distribution, and failure handling.
---

## Overview
Routing is the control plane that maps a request's routing key to the correct shard replica. Robust designs minimize hops, keep shard maps fresh, and degrade safely on partial failures.

## Architectures: client, router, coordinator
- Client library
  - Pros: lowest latency (no middle hop), fewer moving parts per call.
  - Cons: rollout burden for shard map updates, language/SDK surface area.
- Router/gateway (stateless)
  - Pros: centralized policy, simpler clients, easy observability and rate limiting per route.
  - Cons: extra hop; must scale horizontally; careful with retries and timeouts.
- Coordinator (stateful)
  - Pros: supports leases/transactions/locks across shards when required.
  - Cons: added state, availability dependency; avoid unless coordination is needed.

## Catalog: the source of truth
Responsibilities
- Store shard assignments: keyspace → shard → replicas (role, zone), health, capacity, and locality hints.
- Publish a versioned shard map via RPC/HTTP/xDS; include TTLs and ETags for safe caching.
- Provide watch/subscribe APIs for push updates; fall back to polling when needed.

Update model
- Version every map change; clients only apply monotonic versions.
- Stagger refreshes to avoid thundering herds (jitter, backoff, per‑shard invalidations).
- On router startup: fetch map, validate signature/version, warm caches, then accept traffic.

Mermaid: Catalog‑aware routing
```mermaid
flowchart LR
  C[Client] -->|key| RG[Router / Client SDK]
  RG -->|get map (watch)| CAT[(Catalog)]
  RG -->|route(key)| SH1[[Shard 1]]
  RG -->|route(key)| SH2[[Shard 2]]
  SH1 --> P1[(Primary)]; SH1 --> F1[(Follower)]
  SH2 --> P2[(Primary)]; SH2 --> F2[(Follower)]
```

## Shard‑map consistency and safety
- Staleness control: embed `expires_at`/TTL; on expiry, either refresh or fail closed for writes.
- Partial availability: if catalog is unreachable but TTL valid, continue serving; otherwise degrade to read‑only or return retriable errors.
- Map thrash protection: rate‑limit topology changes; batch moves; canary apply to a subset of routers first.

## Failure handling patterns
- Stale map write guard: reject writes if map version < min_required for the target keyspace.
- Retry routing misses with fresh map fetch (bounded attempts and timeouts).
- Circuit breaks per shard and per router→shard path; shed excess early.

Example pseudocode: guarded routing (≤ 20 lines)
```pseudo
def route_and_send(key, op):
  for attempt in 1..2:
    m = shard_map_cache.get()
    if m.expired():
      m = shard_map_cache.refresh()
    shard = m.lookup(key)
    try:
      return send(op, shard.primary)
    except RouteMissError:
      shard_map_cache.refresh()
    except Unavailable as e:
      if attempt == 1: backoff_jitter()
      else: raise e
```

## Production checklist
- Define catalog API (fetch, watch, versioning) and TTL policy.
- Decide router placement (client vs gateway) and retry/circuit breaker budgets.
- Instrument map staleness, lookup errors, and per‑path p95/p99 latency.
- Create runbooks: catalog outage, bad map rollback, router warmup.
