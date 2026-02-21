---
title: Geo-Replication and Conflict Resolution
description: Strategies for multi-region replication—topologies, latency and placement, RPO/RTO, data residency, and conflict detection/resolution (LWW, vector clocks, CRDTs) with diagrams and examples.
---

## Overview
Geo-replication places data copies across regions to reduce user latency and survive regional failures. It introduces WAN latency, higher failure diversity, and potential write conflicts when multiple regions accept writes.

## What, Why, When (and when-not)
What
- Multi-region replication policies (sync, semi-sync, async), placement across regions/AZs, and conflict resolution when writes originate in different regions.

Why
- Reduce cross-region round-trips for reads/writes, meet compliance/data-residency needs, and achieve disaster recovery objectives with bounded RPO/RTO.

When
- Global products with users in multiple continents; strict business continuity; regulatory residency requires data to remain or be primary in-region.

When-not
- Single-region products without DR requirements; strong global linearizability is required but budgets cannot accept WAN latency.

## Core concepts and variants
Topologies
- Regional leader, global followers: one region is write primary; others async followers (low complexity, RPO>0 across regions).
- Per-shard consensus across regions: each shard/range forms a Raft/Paxos group spanning regions; majority commit yields global linearizability (higher latency but strong guarantees).
- Multi-primary (active-active): each region accepts writes; conflicts resolved via LWW, vector clocks, CRDTs, or app logic.

Placement and latency
- Triangle (3-region) majority: commit waits for 2-of-3; tail latency ≈ fastest majority path; place regions to minimize worst-case RTT while maintaining failure independence.
- Semi-sync local-commit: commit after local + 1 in-region or nearby region; remaining replicas async → bounded but non-zero RPO on regional loss.

Data residency
- Keep-at-rest constraints: store/primary in region; replicate summaries/derived data globally. Use field-level or table-level partitioning by residency tag.

Conflict models
- LWW with hybrid clocks: simple but can lose concurrent updates.
- Version vectors/vector clocks: detect concurrency; require resolution policy.
- CRDTs: converge automatically for specific datatypes (sets, counters, registers with causal wins); great for collaborative/mergeable domains.
- Application-specific: domain rules (e.g., higher bid wins, most recent status transition allowed) with reconciliation jobs.

## Design decisions and trade-offs
- Latency vs consistency: global majority commits cost WAN RTTs; async geo-replication lowers latency but raises RPO and staleness.
- Complexity vs safety: CRDTs reduce conflict risk but limit operations; custom resolvers add logic/ops overhead.
- Cost: more regions increase egress/storage; choose minimal set that meets SLOs/residency.

## Architecture and components
- Region-local leaders/followers, cross-region links (private interconnect), time/reference service (e.g., TrueTime) if using bounded staleness, conflict resolver service, CDC for reconciliation.

Mermaid: Two approaches
```mermaid
flowchart LR
  subgraph Global Majority (3 regions)
    R1[Region A] --- R2[Region B]
    R2 --- R3[Region C]
    R1 --- R3
    L1((Leader))
    L1 -->|Append| R2
    L1 -->|Append| R3
    R2 -->|Ack| L1
    R3 -->|Ack| L1
  end
  subgraph Local Commit + Async Geo
    A1[Region A Leader] --> A2[Region B Follower]
    A1 --> A3[Region C Follower]
    note right of A1: Commit after in-region semi-sync
  end
```

## Operational considerations
- Budget RPO/RTO per failure domain; test with regional failover drills.
- Monitor inter-region RTT, packet loss, and replication lag; autoswitch to leader reads during WAN incidents.
- Throttle cross-region backfills; prefer resumeable streaming with checksums/Merkle validation.

## Examples
Example A (quantitative): Commit latency for 3-region majority
- Regions: A↔B 35 ms RTT, B↔C 40 ms, A↔C 70 ms. Majority of 3 needs 2 acks; leader in A sends to B and C.
- Latency ≈ max(leader→B, leader→C) one-way + processing; with pipelining, tail ≈ 35–70 ms network + fsync. Choose leader in the middle (B) to reduce worst-case to ~40 ms.

Example B (architectural): Active-active carts with CRDTs
- Each region accepts cart updates using an OR-Set CRDT (observed-remove). Replicas gossip deltas; merges converge without conflicts. Checkout service reads a consistent snapshot via local leader or majority to prevent double charges.

## Edge cases and anti-patterns
- Using NTP-only unsynchronized clocks for LWW across regions can misorder events; prefer hybrid logical clocks or bounded-clock services.
- Replicating PII cross-border against residency rules; enforce routing/partitioning that keeps sensitive fields local.
- Naive multi-primary on mutable counters without CRDTs leads to lost increments.

## Interactions with adjacent topics
- [Data Partitioning](../03-data-partitioning/): partition by residency/region and align replication accordingly.
- [Consistency & CAP](../05-consistency-and-cap/): bounded staleness vs linearizability under WAN latency.
- [Security & Auth](../12-security-and-auth/): data residency and cross-border controls.

## Production checklist
- Select topology (global majority, local commit + async, or multi-primary with conflict model).
- Define per-region RPO/RTO, staleness budgets, and failover order.
- Implement conflict detection/resolution with metrics on conflict rates and resolutions applied.
- Validate residency via pre-commit policy checks and periodic audits.

## Interview framing checklist
- Given 3 regions and p95 RTTs, which commit policy meets a 60 ms write SLO?
- How would you design conflict resolution for a collaborative document editor?

## References
- Spanner/CockroachDB multi-region docs; Dynamo and Cassandra conflict resolution; CRDT literature (Shapiro et al.)
