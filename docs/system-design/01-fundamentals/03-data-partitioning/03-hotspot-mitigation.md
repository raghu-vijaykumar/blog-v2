---
title: Hotspot Mitigation
description: Detecting and fixing shard/key imbalance with salting, composite keys, time windowing, dynamic replication, admission control, and backpressure.
---

## Overview
Hotspots occur when a small subset of keys or ranges receive disproportionate traffic, saturating a shard's CPU/IO and inflating tail latency. Mitigation combines good key design with dynamic protections and operational controls.

## Detecting and budgeting skew
- Metrics to track
  - Per‑shard: RPS, p95/p99 latency, write bytes/s, storage growth, queue depth, CPU.
  - Per‑key: top‑K hot keys by RPS/bytes; moving average of hottest N keys per shard.
- Skew budget
  - Define an SLO such as: no shard exceeds 2× the median RPS for > 10 minutes, and no single key > 5% of shard RPS for > 1 minute.
  - Alert when exceeded; trigger auto‑mitigation if possible.

## Design‑time techniques
- Salting/bucketing
  - Route by `hash(key) % K` where K ≈ 64–512; map buckets → shards via a ring/catalog.
  - Enables remapping a subset of buckets without moving the entire keyspace.
- Composite keys with time windowing
  - Example: `(month_bucket, hash(user_id)%16)` spreads “latest” writes while maintaining recency locality.
- Key randomization for monotonic inputs
  - Add random or counter‑based suffix/prefix to avoid append‑heavy ranges.
- Pre‑aggregation and write shaping
  - Buffer and batch writes (e.g., roll‑ups per minute) to reduce per‑key QPS bursts; trade latency for stability.

## Dynamic protections
- Hot key read replication (advanced)
  - Temporarily replicate the hottest logical key across multiple shards (or cache tiers); router selects from replicas for reads.
  - Costs: increased write amplification and consistency management.
- Admission control and quotas
  - Per‑key and per‑tenant concurrency/RPS limits with fair queuing.
- Backpressure and retries
  - Bound retries with budgets and jitter; shed excess early at the router.
- Auto split/merge for range shards
  - Split on size or p95 thresholds; merge cold neighbors to control partition count.

Mermaid: Hot key replication for reads
```mermaid
flowchart LR
  C[Client] --> R[Router]
  R -->|key=k| S1[[Shard A]]
  R -->|key=k| S2[[Shard B]]
  subgraph Replication
  S1 --- S2
  end
  note right of R: Router picks least‑loaded replica for reads
```

## Quantitative example: deciding to split
- Given 64 shards with median 2,000 RPS, shard 17 sustains 5,200 RPS for 15 minutes (2.6× median) and p95=180 ms vs SLO 100 ms.
- Action: split 2 hottest buckets owned by shard 17 to a standby shard.
- Expected relief: move ~2/8 of its buckets → ~25% traffic drop; new load ≈ 3,900 RPS; reassess and repeat if still above budget.

## Production checklist
- Track per‑shard skew ratio (max/median) and top‑K keys; alert on budget breaches.
- Implement bucket/vnode mapping to enable partial remaps.
- Define policies for hot key replication and auto split/merge triggers.
- Enforce retry budgets and admission control at router.
- Maintain runbooks: enable/disable read replicas for a key, throttle adjustments, targeted bucket remap.
