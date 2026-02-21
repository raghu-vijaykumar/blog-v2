---
title: Partitioning Policies
description: Range, hash, list, and composite partitioning with guidance on selection, routing keys, buckets vs physical shards, and worked examples.
---

## Overview
Partitioning policy determines how keys map to partitions. The right choice depends on access patterns (point reads vs ranges), key cardinality, locality needs, and growth shape.

## Policies and when to use them
- Range
  - Use when range scans and time‑ordering are first‑class. Beware monotonic hotspots; add split/merge automation.
  - Pros: locality for ranges/time windows; efficient min/max/top‑N within a range.
  - Cons: hotspot risk on latest; operational overhead of splits/merges.
- Hash
  - Use when requests are mostly single‑key and you want uniform distribution.
  - Pros: simple and balanced; easy capacity planning.
  - Cons: poor range locality; scatter‑gather for range/attribute queries.
- List/Set
  - Use for policy partitions like region, tier, or customer class.
  - Pros: compliance and affinity control; targeted maintenance windows.
  - Cons: manual balancing; uneven groups can skew.
- Composite (recommended default)
  - Combine dimensions: time window + hash(user_id), region + hash(tenant_id), etc.
  - Pros: balances hotspots while preserving useful locality.
  - Cons: slightly more complex routing and schema.

## Buckets vs physical shards
- Introduce K logical buckets (e.g., 128–1024) from the policy (range slices or hash buckets).
- Maintain a mapping K → M physical shards (nodes) via a ring or catalog. Changing M only remaps a fraction of K.

Mermaid: Policy → Buckets → Shards
```mermaid
flowchart LR
  A[Key] --> P[Policy (e.g., hash(key) or range)]
  P --> B[Logical Bucket (0..K-1)]
  B --> R[Ring/Catalog]
  R --> S1[[Shard A]]
  R --> S2[[Shard B]]
```

## Worked examples
Example 1 (time‑series orders)
- Access: recent month heavy, queries by tenant and recency.
- Policy: composite (month_bucket, hash(tenant_id)%16).
- Benefit: latest traffic spreads across many buckets; old months can be merged.

Example 2 (global SaaS regions)
- Access: users sticky to region; admin queries per tenant.
- Policy: list(region) × hash(tenant_id)%64.
- Benefit: sovereignty and latency control; even spread per region.

## Selection checklist
- Are range scans primary? Prefer range/composite with a time window.
- Are point lookups primary? Prefer hash or composite.
- Do you need regional/tenant policy control? Add list/set at the front of the key.
- Choose bucket count K high enough for growth (≥ 10× planned shards) but not so high that metadata becomes unwieldy.

## Production checklist
- Define routing key fields and ensure immutability.
- Fix bucket count K (or choose vnode scheme) and document remap strategy.
- Establish split/merge thresholds for range partitions (size, RPS, p95).
- Add canaries and backfill throttles for policy changes.
