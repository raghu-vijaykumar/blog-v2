---
title: Selection Guide & Comparisons
description: Practical decision frameworks for choosing databases and storage—map workload to data model and engine, compare SQL/NoSQL/NewSQL and object/block tiers, with a concise decision flow, trade-offs, and worked examples.
---

## Overview
This guide helps you pick a primary database and supporting stores for a given workload. It blends workload profiling (access patterns, latency SLOs), data modeling constraints (transactions, joins), and operational context (team skills, managed options) into a repeatable decision flow.

## What, Why, When (and when-not)
What
- A step-by-step decision flow and comparison guidance across common database families and storage tiers.

Why
- Prevent cargo-cult choices and overfitting one engine to all use cases. Make explicit the trade-offs among latency, consistency, cost, and operability.

When
- New service design, major scale inflection, or replatforming. Also useful for isolating read-heavy features via derived stores.

When-not
- Premature optimization for tiny workloads; start simple with a well-understood relational engine before fragmenting.

## Core concepts and variants
Workload profiling (minimum data to decide)
- Data model: strict relational constraints vs aggregate-document vs KV/time-series vs graph.
- Access patterns: top 5 queries; ratio of point lookups vs range scans vs aggregations.
- Scale targets: p95/p99 latency SLOs; QPS and write rate; dataset size and growth; multi-region needs.
- Consistency: read-after-write guarantees; cross-entity invariants; acceptable staleness.

Decision flow (high level)
1) Do you need cross-entity, transactional joins and strong integrity? Prefer relational/Distributed SQL.
2) Are writes heavy, append-oriented with simple primary-key reads? Prefer KV/LSM-backed stores.
3) Are queries scan/aggregate-heavy with low update frequency? Prefer columnar warehouses/lakes.
4) Is the primary query domain global text/search? Use search systems; pair with a source of truth DB.
5) Multi-region low-latency writes? Consider multi-primary/leaderless or Distributed SQL; accept conflict/latency trade-offs.

Engine comparisons (concise)
- Relational (PostgreSQL/MySQL): +ACID/joins/tooling; −scale-out without sharding; sweet spot for OLTP.
- Distributed SQL (Spanner/Cockroach/Yugabyte): +SQL+scale-out; −consensus latency, ops complexity.
- Document (MongoDB/Couchbase): +aggregate flexibility; −cross-doc joins/transactions vary.
- KV/Wide-column (DynamoDB/Cassandra/Bigtable): +elastic writes; −limited query patterns, modeling critical.
- Time-series (Timescale/ClickHouse/Influx): +ingest/retention; −complex joins.
- Columnar (ClickHouse/Snowflake/BigQuery): +scans/aggregates; −row updates.

Storage tier comparisons
- Block (NVMe/EBS/PD): +low-latency random IO; −cost per GB; IOPS limits.
- Object (S3/GCS/Azure Blob): +durability/cost/throughput; −latency; eventually consistent behaviors.

```mermaid
flowchart TD
  A[Start: Profile workload] --> B{Strong cross-entity\n  transactions?}
  B -- Yes --> SQL[Relational / Distributed SQL]
  B -- No --> C{Write-heavy, append-\n  oriented?}
  C -- Yes --> KV[KV / Wide-Column (LSM)]
  C -- No --> D{Scan/aggregate-heavy?}
  D -- Yes --> COL[Columnar Warehouse]
  D -- No --> DOC[Document / Mixed]
  SQL --> AUX[Derived: Search / MVs / Replicas]
  KV --> AUX
  COL --> AUX
  DOC --> AUX
```

## Design decisions and trade-offs
- One primary + many derived stores vs polyglot primaries: centralize writes for integrity; derive read-optimized views/search; avoid multi-primary truth unless justified.
- Managed vs self-managed: managed reduces toil but constrains tuning; self-managed increases control and responsibility.
- Multi-region: accept latency of consensus (Distributed SQL) or embrace conflict models with CRDTs/Sagas.

## Architecture and components
- Primary OLTP store; CDC to search/analytics/MVs; object storage for backups/lakes; routers and connection pools; observability stack.

## Operational considerations
- Team skill and on-call maturity; cost modeling for peak vs average; migration paths and rollback strategies.

## Examples
Example A (quantitative): Cost/latency trade-off for OLTP choice
- Option 1: Managed PostgreSQL (single-region) with read replicas. Target p95 write < 20 ms, read < 15 ms. Instance + storage ≈ $X/month (plug provider).
- Option 2: Distributed SQL across 3 regions; consensus adds ~30–60 ms floor for cross-region writes. Higher cost and complexity; justified only if local-write multi-region or per-tenant regionality is required.
- Decision: If 95% traffic is single-region and strict multi-region writes aren’t required, pick Option 1 and replicate async for DR.

Example B (architectural): SaaS multi-tenant platform
- Primary: Relational DB sharded by tenant_id (see [Partitioning](../03-data-partitioning/)).
- Derived: Search index for global discovery; columnar warehouse for analytics; per-tenant read replicas for isolation.
- Benefits: Integrity on writes; scalable reads/search; clear staleness budgets.

## Edge cases and anti-patterns
- Over-using one engine (e.g., KV) for ad-hoc analytics; forcing text search into OLTP; global ACID across unrelated aggregates.

## Interactions with adjacent topics
- [Families & Workloads](./01-families-and-workloads.md) for model mapping.
- [Storage Engines](./02-storage-engines-and-data-structures.md) for WA/RA budgets.
- [Replication](../04-replication/) and [Consistency](../05-consistency-and-cap/) for durability and staleness.
- [Search & Indexing](../14-search-and-indexing/) for discovery use cases.

## Production checklist
- Document top access patterns and SLOs; pick primary store accordingly.
- Define derived stores and freshness budgets; set CDC/MV pipelines.
- Choose managed vs self-managed; plan DR and backup/PITR.

## Interview framing checklist
- Walk through your selection for a given workload and justify trade-offs.
- How do you avoid overfitting one database to all features?

## References
- Vendor architecture guides; DDIA ch.1–3; cloud provider storage tier docs; Martin Fowler on Polyglot Persistence
