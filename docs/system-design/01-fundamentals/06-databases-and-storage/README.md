---
title: Databases & Storage
description: Families (SQL/NoSQL/NewSQL), OLTP vs OLAP, storage engines (B-Tree/LSM/columnar), indexing and transactions (MVCC, isolation), storage tiers (block/object/file), materialized views and read replicas, with production guidance and selection frameworks.
---

# Databases & Storage

## Overview
Databases persist state and serve queries under latency, durability, and scale constraints. Choosing and operating the right store depends on workload shape (OLTP vs OLAP), data model (relational, document, key‑value, time‑series, graph), storage engine internals (B‑Tree vs LSM vs columnar), and infrastructure tiers (NVMe/SSD, network block, object storage). This module provides a pragmatic map and production checklists.

## What, Why, When (and when‑not)
What
- Core families (SQL, NoSQL, NewSQL), engines (B‑Tree/LSM/columnar), and storage tiers (block/object/file). Adjacent techniques: indexes, MVCC/transactions, materialized views, replicas.

Why
- Match data model and engine to workload to achieve predictable latency, cost efficiency, and operability. Internal mechanics determine write/read paths, compaction, and failure behavior.

When
- OLTP with transactional integrity (orders, payments) → row stores with B‑Tree or LSM, strong per‑entity ordering.
- Write‑heavy, append‑only, high cardinality (events, time‑series) → LSM KV or specialized time‑series DB.
- Analytical scans/aggregations over large datasets → columnar stores/warehouses on object storage.

When‑not
- Avoid general‑purpose RDBMS for petabyte‑scale analytics; avoid global ACID across unrelated aggregates in high‑scale OLTP; avoid LSM for mostly random point reads with low write rates if B‑Tree fits and simplifies ops.

## Module contents
- [01 — Families & Workloads](./01-families-and-workloads.md): SQL/NoSQL/NewSQL; OLTP vs OLAP; common access patterns.
- [02 — Storage Engines & Data Structures](./02-storage-engines-and-data-structures.md): B‑Tree, LSM, heap/row vs columnar; Bloom filters; compaction.
- [03 — Indexing, Transactions & Isolation](./03-indexing-transactions-and-isolation.md): indexes, MVCC, isolation levels, anomalies.
- [04 — Storage Tiers & Durability](./04-storage-tiers-and-durability.md): block/object/file; WAL and snapshots; RAID/erasure coding; latency/cost.
- [05 — Materialized Views, Read Replicas & CQRS](./05-materialized-views-read-replicas-and-cqrs.md): read scaling, freshness, denormalization.
- [06 — Backup, Restore & PITR](./06-backup-restore-and-pitr.md): logical vs physical, snapshots, RPO/RTO planning.
- [07 — Operations, Observability & Runbooks](./07-operations-observability-and-runbooks.md): capacity, tuning, compaction/vacuum, connection pools.
- [08 — Selection Guide & Comparisons](./08-selection-guide-and-comparisons.md): decision matrix with examples.
- [09 — Case Studies](./09-case-studies.md): engine choices and operational lessons.

## Interactions with adjacent topics
- See [Data Partitioning](../03-data-partitioning/) for sharding and shard‑map design.
- See [Replication](../04-replication/) for topologies and propagation.
- See [Consistency & CAP](../05-consistency-and-cap/) for guarantees and read policies.
- See [Search & Indexing](../14-search-and-indexing/) for global discovery queries.

## Production checklist (quick)
- Define workload: read/write ratios, access patterns, latency SLOs, growth rate, retention.
- Choose family and engine aligned to access patterns (point lookups vs scans; range vs aggregation).
- Pick durability path (WAL/snapshot) and backup/PITR strategy to meet RPO/RTO.
- Plan capacity and tiering (hot NVMe vs warm/cold object storage); budget compaction/vacuum IO.
- Establish observability (queues/compaction lag, buffer cache hit rate, p95/99 latencies, errors).

Proceed to the sub‑pages for detailed guidance and worked examples.
