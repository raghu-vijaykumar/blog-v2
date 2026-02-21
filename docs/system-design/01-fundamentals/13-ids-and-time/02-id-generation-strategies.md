---
title: ID Generation Strategies — Centralized vs Decentralized
description: How to issue IDs at scale - sequences, Snowflake-like, random/sortable client-side IDs, collision math, and HA considerations.
---

## Overview
ID generation must balance uniqueness guarantees, throughput, ordering needs, and failure isolation. You can centralize issuance (simple control, added latency) or decentralize it (low latency, excellent scale) with careful design to avoid hotspots and collisions.

## What, Why, When (and when-not)
What
- Approaches for generating unique identifiers: database sequences, dedicated ID services, or client-side libraries (random/sortable composites).

Why
- To eliminate write-time coordination and enable horizontal scale while meeting ordering, dedup, and forensics needs.

When
- Always needed for distributed persistence of entities/events. Ordering-heavy workloads may prefer sortable schemes.

When-not
- Single-node systems or prototypes where autoincrement is acceptable and a bottleneck is unlikely.

## Core concepts and variants
- Centralized issuance: DB sequences (AUTO_INCREMENT), Redis INCR, or bespoke ID service. Strong ordering but creates a point of contention.
- Snowflake-style: time bits | region/worker | per-worker sequence. Sortable; decentralizes issuance; requires safe worker-ID assignment/fencing.
- Random (UUIDv4): 122 bits of randomness; extremely low collision probability; ideal for uniform sharding; not sortable.
- Time-ordered random (UUIDv7/ULID/KSUID): timestamp prefix + randomness for lexicographic sort; may cause temporal hotspots without suffix hashing.

## Design decisions and trade-offs
- Latency path: central issuance adds network hop and dependency availability; client-side generation is near-zero latency.
- Ordering: global monotonicity is hard without centralization; per-partition monotonicity is usually enough.
- Hotspot risk: time-ordered IDs cluster; add hash/randomized suffix or shard on hashed key.
- Operational surface: an ID service must be HA (multi-node, leader election, fencing, state checks).

## Algorithms and policies (conceptual)
Pseudocode: Snowflake compose (≤ 25 lines)
```pseudo
const EPOCH = 1704067200000  # custom epoch (ms)
bits: time=41, region=5, worker=5, seq=12
state: last_ts=-1, seq=0

function next_id(now_ms, region_id, worker_id):
  ts = now_ms - EPOCH
  if ts < 0: error("clock before epoch")
  if ts == last_ts:
    seq = (seq + 1) & ((1<<12)-1)
    if seq == 0: wait_until_next_millis()
  else:
    seq = 0
  last_ts = ts
  return (ts << (5+5+12)) | (region_id << (5+12)) | (worker_id << 12) | seq
```

Policy notes
- Assign worker IDs via a coordination service (e.g., etcd/ZooKeeper) with fencing tokens to avoid duplicate workers after split-brain.
- If the clock moves backwards, either stall until caught up or switch to a clock-regression safe path (e.g., increment sequence in a reserved range and emit metric).

## Architecture and components
- Central ID service: stateless API nodes + persistent counter store; multi-leader with CRDT counters or single-leader with failover.
- Decentralized libraries: link-time or runtime packages; optionally validate IDs at gateways.
- Coordination: registry for worker IDs; health checks; fencing tokens to prevent reuse.

## Operational considerations
- SLOs: p99 issuance latency, availability of the issuance path, and collision budget (per 10^12 IDs).
- Skew budgets: timestamped IDs assume NTP/PTP within X ms; alert on p99 > 500 ms.
- Sequence wrap-around: with 12-bit per-ms sequence, max 4096 IDs/ms/worker; size fleet accordingly.

## Examples
Example A (quantitative): UUIDv4 collision odds
- With 122 random bits, the birthday bound suggests first collision around 1.2×2^(122/2) ≈ 2.7×10^18 IDs. At 1M IDs/s continuously, you’d run for ~85 years before reaching that scale.

Example B (architectural): HA Snowflake service
- Three nodes behind a load balancer; etcd provides worker IDs with 60s leases. If a node restarts, it must reacquire a new worker ID. Cloud clocks are NTP-disciplined; code stalls if now < last_ts.

## Edge cases and anti-patterns
- Duplicate worker IDs after a network partition; fix with fencing tokens and short leases.
- Ignoring clock regressions; add monotonic guards and metrics.
- Relying on central DB sequences at 100k rps; becomes a bottleneck and single point of failure.

## Interactions with adjacent topics
- Consistency & CAP — Clocks and Ordering: ../05-consistency-and-cap/05-clocks-and-ordering.md
- Availability — Failover and Fencing: ../09-availability-and-fault-tolerance/06-failover-promotion-and-dr.md

## Production checklist
- Pick strategy per workload; document properties (order, collision budget, throughput per node).
- Enforce worker-ID fencing; handle clock regressions.
- Set alerts for issuance latency, error rate, and skew.

## Interview framing checklist
- Compare UUIDv4 vs UUIDv7 vs Snowflake for a multi-region service at 200k rps.
- How do you prevent duplicate workers in Snowflake-like designs?

## References
- Twitter Snowflake posts; RFC 4122; draft UUIDv7; etcd/ZooKeeper fencing patterns.
