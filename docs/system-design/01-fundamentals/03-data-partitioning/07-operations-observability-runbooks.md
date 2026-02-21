---
title: Operations, Observability & Runbooks
description: SLOs, metrics, dashboards, alerting, and practical runbooks for sharded data platforms.
---

## Overview
Operating a sharded datastore requires per‑shard visibility, clear SLOs, and rehearsed runbooks for capacity, skew, and topology changes.

## SLOs and capacity targets
- Per‑shard p95 latency (read/write), error rate, and throughput ceilings.
- Target shard size (e.g., 200–400 GB hot data) and replica lag budgets.

## Metrics and dashboards
- Per‑shard: RPS, latency p50/p95/p99, CPU, IO, queue depth, replication lag, storage growth.
- Global: skew ratio (max/median), shard‑map freshness (age/TTL), backfill throughput, route misses.
- Dashboards: shard heatmap, hot key top‑K, rebalancing progress, retry and circuit breaker rates.

## Alerts
- Skew budget exceeded, replica lag above threshold, map TTL expired, backfill stalled, error budget burn rates.

## Runbooks (examples)
- Tenant move: select buckets, initiate backfill, monitor lag, cutover, verify, drain source.
- Hot range split: detect threshold breach, split, remap, verify, merge checks for cold ranges later.
- Node add/remove: seed replicas, remap vnodes/buckets incrementally, monitor saturation and p95.
- Bad map rollback: revert to last good version, invalidate caches, reissue fetch.

## Production checklist
- Define on‑call dashboards and drill paths; test alerts with game days.
- Keep “how to” runbooks versioned with clear abort/rollback steps.
- Enforce retry budgets and backpressure at routers to protect shards.
- Periodically validate shard balance and rebalance before SLO erosion.
