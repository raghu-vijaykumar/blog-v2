---
title: Consistency Case Studies
---

# Consistency Case Studies

## Overview
Real systems combine multiple consistency models. This page distills how popular databases and platforms implement guarantees, and how product workloads map to CP/AP choices.

## Systems
- Google Spanner / Cloud Spanner
  - Model: Per‑range consensus with TrueTime; external consistency (linearizable) writes; bounded‑staleness and strong reads available.
  - Knobs: read modes (strong, exact‑staleness, bounded); replica placement; leader locality.
  - Pitfalls: higher cross‑region RTT; write throughput bound by consensus and commit waits for ε in some paths.

- CockroachDB
  - Model: Per‑range Raft with hybrid logical timestamps; serializable transactions; follower reads with closed timestamps.
  - Knobs: closed timestamp interval; read_only optimization; locality constraints.
  - Pitfalls: contention hotspots; long transactions hitting timestamp cache; global tables trade latency for consistency.

- Cassandra / Dynamo‑style stores
  - Model: Leaderless, tunable consistency with R/W quorums; LWW timestamps; anti‑entropy and read repair.
  - Knobs: consistency level per request (ONE, QUORUM, ALL), hinted handoff, repair cadence.
  - Pitfalls: clock skew affects LWW; R+W≤N reads stale; repairs must be routine.

- MongoDB
  - Model: Primary/secondary; snapshot‑isolation‑like behavior per document; read preferences (primary, primaryPreferred, secondary, secondaryPreferred); causal consistency option in drivers.
  - Knobs: write concern (w:1, majority), read concern (local, majority, linearizable), session/causal consistency.
  - Pitfalls: secondary reads are stale without read concern majority; linearizable reads require specific pattern and higher latency.

- PostgreSQL / MySQL with replicas
  - Model: Primary/follower; strong/serializable per‑primary (config‑dependent); follower reads are stale; semi‑sync options reduce loss.
  - Knobs: synchronous_commit, replica apply delay, hot_standby_feedback; read routing; fencing and reparenting tooling (e.g., Patroni, Orchestrator, Vitess).
  - Pitfalls: lag spikes during checkpoints/IO; split‑brain risks without fencing; RYW violations on follower reads.

- Redis with replicas
  - Model: Async replication; eventual consistency; optional WAIT (ack count) offers durability hints; client‑side RYW via sticky sessions.
  - Pitfalls: failover data loss window; reads from replicas are stale; WATCH/MULTI semantics local to primary.

## Product mappings
- SaaS multitenant app
  - Strong writes for identity/ACLs/billing (CP). Bounded‑staleness or follower reads for dashboards and catalogs with RYW tokens. Caches with versioned keys and invalidation via CDC.

- Social feed
  - AP for writes (fan‑out eventually), causal or RYW for user timeline; counters via CRDTs; search/graph queries from derived stores.

- Payments/ledger
  - CP with serializable/linearizable transactions; idempotent operations; dual writes avoided; caches read‑through with strict invalidation.

## Examples

Example A (quantitative): Cassandra quorum choice
- N=3 across 3 AZs. Strongish behavior: set W=R=QUORUM→R+W=4>3. Per‑AZ p95=8 ms; tail latency ~ 2nd‑fastest ≈ 9–12 ms. With ONE/QUORUM (W=1,R=2), writes are faster but risk stale reads if a single AZ fails right after write.

Example B (architectural): Spanner bounded‑staleness global reads
- Frontend uses exact_staleness=2s for global search results, avoiding cross‑region leader reads; checkout path uses strong reads+writes to ensure correctness.

## Edge cases and anti‑patterns
- Declaring a system “CP or AP” globally; instead, split by operation/domain.
- Relying on clocks for LWW without monitoring skew.
- Skipping regular repairs in leaderless systems leading to entropy growth.

## Interactions with adjacent topics
- [CAP and PACELC](./02-cap-and-pacelc.md)
- [Quorums and Read Policies](./03-quorums-and-read-policies.md)
- [Session Guarantees](./04-session-guarantees-and-client-techniques.md)

## Production checklist
- Choose per‑operation guarantees; verify vendor/database knobs match intent.
- Build consistency probes per system (e.g., RYW on Mongo secondaryPreferred).
- Document trade‑offs for oncall; include fallback/promotion rules.

## Interview framing checklist
- Compare two systems (e.g., Cassandra vs Spanner) for a geo‑write workload; justify choice.
- Propose CP/AP split for an e‑commerce app and quantify latency impacts.

## References
- Vendor docs: Spanner, CockroachDB, Cassandra, MongoDB, Postgres/MySQL, Redis
- DDIA; Jepsen reports on the above systems
