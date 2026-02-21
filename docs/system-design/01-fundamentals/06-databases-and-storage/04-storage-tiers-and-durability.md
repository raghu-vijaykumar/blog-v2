---
title: Storage Tiers & Durability (Block, Object, File; WAL, Snapshots)
description: Choosing between block, object, and file storage; durability paths (WAL, snapshots, RAID/erasure coding); latency and cost trade-offs; production patterns with examples and checklists.
---

## Overview
Database performance and resilience depend on the underlying storage tier and durability strategy. Pair the engine’s write path (WAL, checkpoints/flushes) with appropriate media (local NVMe, network block, object storage) and redundancy (RAID/erasure coding) to meet SLOs at sustainable cost.

## What, Why, When (and when-not)
What
- Storage tiers: local NVMe/SSD, network block (EBS/PersistentDisk), object (S3/GCS/Azure Blob), and shared/network file systems.
- Durability mechanisms: WAL + fsync, snapshots (LVM/ZFS/volume-level), logical dumps, RAID/erasure coding.

Why
- Latency and throughput of the tier set floors for commit latency and compaction/checkpoint windows; durability determines RPO/RTO and failure blast radius.

When
- OLTP primaries → local NVMe or high-IOPS block with strict fsync semantics.
- Read replicas / analytics → cheaper block or object-backed columnar stores.
- Backups and archival → object storage with lifecycle policies and cross-region replication.

When-not
- Don’t run WAL on slow network filesystems without stable latency. Avoid RAID-5/6 for high write OLTP without controller cache; prefer mirroring/striping with battery-backed cache or multi-volume striping.

## Core concepts and variants
Block vs object vs file
- Block: low-latency random IO; consistent fsync semantics; per-volume IOPS limits. Good for databases.
- Object: high-throughput, high-latency; excellent durability; eventual consistency (improved with features). Good for backups, columnar lakes.
- Network/shared FS: convenience for HA failover mounts; can introduce unpredictable latency.

WAL, checkpoints, snapshots
- WAL: append-first write path; fsync on commit or group commit.
- Checkpoints: flush dirty pages to limit recovery time.
- Snapshots: crash-consistent point-in-time copies at volume/FS level; pair with WAL to achieve PITR.

Redundancy
- RAID 1/10 (mirroring/striping) for OLTP; RAID 5/6 increase write WA. Erasure coding for object stores.

## Design decisions and trade-offs
- Latency vs cost: NVMe > network block > HDD > object; choose per tier (primary vs replica vs backup).
- Consistency semantics: ensure correct fsync barriers across hypervisors/cloud volumes; test crash safety.
- Snapshots vs logical dumps: snapshots are faster but engine-agnostic consistency requires quiescing; logical dumps portable but slower and larger CPU.

## Architecture and components
- Primary: WAL on fast media, data on fast or balanced media; snapshots coordinated with engine checkpoints.
- Backups: snapshot to object storage; optional cross-region replication.

```mermaid
flowchart LR
  DB[(Database)] --> WAL[WAL (Fast NVMe)]
  DB --> DATA[(Data Files / Pages)]
  subgraph Durability
    SNAP[Volume Snapshot] --> OBJ[(Object Storage: Backups)]
  end
  DATA --> SNAP
  WAL --> SNAP
```

## Operational considerations
- Pre-warm volumes and disable write cache lying; verify barriers. Measure fsync latency p99.
- Snapshot orchestration: coordinate with low-traffic windows; record WAL position/LSN alongside snapshot metadata.
- Tiering: move cold partitions/older segments to cheaper storage (columnar/object) with clear freshness SLAs.

## Examples
Example A (quantitative): WAL bandwidth and storage choice
- Peak writes: 12k/s × 1 KB/row ≈ 12 MB/s WAL. With group commit 5 ms, batches ~60 ops/commit. Choose gp3-like block volume with 16k IOPS and 500 MB/s throughput; p99 fsync < 4 ms to keep p95 latency < 20 ms.

Example B (architectural): Tiered analytics
- OLTP on NVMe-backed block; hourly snapshot + WAL shipping to object; columnar warehouse reads snapshots directly from object storage; RPO 15 minutes via incremental WAL segments.

## Edge cases and anti-patterns
- Snapshots without WAL coordination → restore boots to inconsistent state. Network FS with unstable latency for WAL → tail spikes. Ignoring cloud volume burst credits → sudden throttling.

## Interactions with adjacent topics
- [Replication](../04-replication/) for follower apply rates and storage layout.
- [Consistency & CAP](../05-consistency-and-cap/) for staleness allowed on object-backed analytics.
- [Partitioning](../03-data-partitioning/) for tiering old partitions.

## Production checklist
- Measure fsync p95/p99 and sustained WAL throughput; provision IOPS/throughput with 30–50% headroom.
- Implement coordinated snapshots + WAL archiving; store LSN/metadata with backups.
- Test crash safety and restore drills quarterly.

## Interview framing checklist
- How to design storage for OLTP primaries vs replicas? Snapshot + PITR plan?
- Trade-offs of RAID levels and object storage for databases?

## References
- PostgreSQL/MySQL durability docs; AWS EBS/EFS, GCP PD/Filestore, Azure Disk/Files docs; ZFS/Btrfs snapshots; S3/GCS durability guarantees
