---
title: Replication Case Studies
description: Practical case studies across OLTP databases, leaderless KV stores, and multi-region SaaS—design choices, incident learnings, and quantitative sizing.
---

## Overview
Representative designs and lessons learned from real-world replication deployments. Use these to ground trade-offs in numbers and failure stories.

## Case study A: OLTP Postgres (N=3 per shard) with semi-sync
Context
- Per-tenant sharded OLTP; p95 write < 15 ms SLO; reads 4x writes, follower reads allowed with 200 ms staleness.

Design
- Topology: 1 leader, 1 semi-sync follower in same AZ, 1 async follower in another AZ.
- Commit: leader fsync + semi-sync ack ⇒ median added latency ~2–4 ms.
- Failover: Patroni + etcd; fencing via leader key epoch; routers enforce epoch on writes.

Operations
- Dashboards on lag, WAL queue, apply errors. DR drills monthly. Rebuild followers from nightly base snapshot + continuous WAL.

Incident lesson
- Storage firmware regression doubled fsync p95; semi-sync acks slowed and write SLOs breached. Mitigation: hedge to both followers; accept either ack; raise alerts on fsync.

Quantitative
- Peak 12k tx/s writes; semi-sync follower apply 15k tx/s; async follower 20k tx/s. Median lag < 80 ms; p99 < 300 ms under peak.

## Case study B: Cassandra leaderless KV (RF=3)
Context
- Session store and feature flags; low write latency, high availability; occasional staleness acceptable.

Design
- Tunable quorums: W=2, R=1 for writes; R=QUORUM for config reads. Read repair enabled; hinted handoff for transient failures.

Operations
- Anti-entropy (Merkle trees) weekly; compaction tuned to reduce write amplification. Latency-aware snitching for replica selection.

Incident lesson
- R=1 used for a critical path read caused stale config during a partial partition. Policy updated to R=QUORUM for safety-critical keys; caching added.

Quantitative
- RF=3; tolerate 1 failure and continue R/W. 99p write latency 6 ms; reads at R=QUORUM 9 ms.

## Case study C: Multi-region active-active carts with CRDTs
Context
- E-commerce carts across 3 regions; local writes required; consistency at checkout must prevent double-charge.

Design
- OR-Set CRDT for adds/removes; periodic delta-gossip between regions. Checkout performs a majority read to capture a consistent snapshot and finalizes the order.

Operations
- Conflict rate tracked; alerts if merge error > threshold. Canary region can be isolated without data loss.

Incident lesson
- Clock skew led to unexpected LWW behavior on a non-CRDT field (note). Migrated that field to a CRDT register with causal wins.

Quantitative
- Inter-region RTTs 35–70 ms; convergence within 2–3 RTTs after bursts. Checkout majority read p95 ~45 ms.

## Production checklist (case-study-driven)
- Verify SLO math with measured fsync and WAN RTTs; model tail latencies.
- For leaderless, align R/W quorum to criticality; never use R=1 for safety-critical data.
- For active-active, choose CRDTs where possible; otherwise implement explicit resolution logic with metrics.

## References
- Postgres/Patroni; Cassandra/Dynamo; CRDT research and practical guides from Riak and Redis CRDT modules
