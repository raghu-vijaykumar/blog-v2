---
title: IDs — Fundamentals and Properties
description: Uniqueness, entropy, sortability, sharding-friendliness, human readability, and how ID choices affect correctness and scale.
---

## Overview
IDs are compact representations of identity and order. The right scheme makes sharding, indexing, and deduplication cheap while preserving correctness under retries and failures.

## What, Why, When (and when-not)
What
- A policy for generating globally unique (or near-unique) identifiers with known probability of collision, distribution properties, and optional time-order.

Why
- IDs determine shard placement, index structure, and dedup semantics. Poor choices create hotspots, fragmentation, or operational failure modes.

When
- Whenever entities or events must be uniquely referenced across processes/regions, or when write ordering/range scans matter.

When-not
- Single-process prototypes where natural keys or autoincrement counters suffice and distribution is irrelevant.

## Core concepts and variants
- Uniqueness probability: strength set by entropy (bits). 128-bit random IDs make birthday collisions negligible for most workloads.
- Sortability: time-ascending lexicographic order aids feeds and range scans; can introduce temporal hotspots without suffix randomization.
- Locality: portion of the ID that encodes time/region/rack can aid debugging and forensics; beware leakage of sensitive metadata.
- Sharding-friendliness: uniform distribution across shards minimizes hot partitions; hash prefixes help for sortable IDs.
- Readability/Length: Base32/Base58 encodings improve readability; compact binary storage is fastest in databases.

## Design decisions and trade-offs
- Centralized sequences vs decentralized generation: sequences are simple but may require HA and can bottleneck; decentralized IDs scale but must control hotspots and collision risk.
- Human-facing formats (Base58/ULID) vs binary storage: human-friendly is great at edges, but convert to binary in storage to save space and index height.
- Embedding metadata (region/time) improves operations but can leak information; use only non-sensitive scopes and document privacy posture.

## Algorithms/policies (conceptual)
- Random IDs: secure RNG; 128-bit. No coordination, uniformly distributed writes; no inherent order.
- Sortable composite: timestamp bits | randomness; provides coarse time order with uniform shard spread if low bits are hashed/randomized.

## Architecture and components
- Client libraries generate IDs close to producers; gateways validate format if needed. Storage services consume as opaque keys and index appropriately.
- Optional centralized ID service for Snowflake-like sequences with leader election and fencing.

## Operational considerations
- SLOs: collision probability per 10^12 IDs, p99 shard imbalance, and ID service availability (if centralized).
- Metrics: shard key distribution, max partition QPS vs median, index height/fragmentation, and RNG health.

## Examples
Example A (quantitative): Birthday bound for 128-bit random IDs
- Expected first collision scale ~ 1.2×2^(b/2). For b=128, ~1.2×10^19 IDs; effectively zero risk at 10^12/year scale.

Example B (architectural): Opaque IDs at the edge, binary in storage
- Public APIs accept Base32 ULIDs (26 chars) for readability; backend stores 16-byte binary IDs for compact B-tree indexes and faster joins.

## Edge cases and anti-patterns
- Autoincrement IDs across shards cause collisions or require central DB writes for issuance; avoid for cross-region systems.
- Embedding PII in IDs (e.g., email hash) can leak user info; treat IDs as opaque, non-sensitive.

## Interactions with adjacent topics
- Consistency & CAP — Clocks and Ordering: ../05-consistency-and-cap/05-clocks-and-ordering.md
- Data Partitioning — Routing and Hotspot Mitigation: ../03-data-partitioning/01-routing-and-catalogs.md, ../03-data-partitioning/03-hotspot-mitigation.md

## Production checklist
- Define required properties: uniqueness, order, length, human-friendliness, and sharding distribution.
- Choose binary storage for databases; document public encoding for APIs.
- Set metrics and alerts for shard imbalance and RNG/ID service health.

## Interview framing checklist
- Compare random vs sortable vs sequence IDs and when to choose each.
- Explain collision math and how to detect/mitigate hotspots.

## References
- RFC 4122 (UUID), draft UUIDv7; ULID/KSUID specs; Twitter Snowflake posts.

## Diagram
```mermaid
flowchart LR
  subgraph Producers
    A[Client/App] -->|generateId()| G[ID Library]
  end
  G -->|opaque ID| S[(Storage/Index)]
  G -->|idempotency key| R[Redis/Cache]
  S --> Q[Queries/Scans]
```
