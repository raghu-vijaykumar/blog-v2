---
title: Case Studies
description: Practical partitioning designs across SaaS multitenancy, time‑series telemetry, and marketplace workloads.
---

## Overview
Concrete designs illustrating routing keys, policies, shard counts, and operational playbooks.

## SaaS multitenancy (tenant_id on a hash ring)
- Routing key: `tenant_id`
- Policy: hash with 256–1024 buckets; consistent hashing ring with vnodes.
- Initial size: 48 shards; K=512 buckets → ~10–11 buckets/shard.
- Risks: “mega tenants” skew; Mitigations: per‑tenant quotas, sub‑bucketing for hot tenants, read replicas.

## Time‑series telemetry (device × day)
- Routing key: `(day_bucket, hash(device_id)%32)`
- Policy: composite range(time) × hash(device).
- Benefits: today’s load spread across many buckets; old days merged/tiered.
- Risks: bursty fleets; Mitigations: ingest buffers, backpressure, nightly compaction.

## Marketplace orders (seller‑centric with regional lists)
- Routing key: `(region, hash(seller_id)%64)`
- Policy: list(region) × hash(seller_id).
- Reads: seller dashboards (single shard), buyer searches via external search.
- Risks: seasonality hotspots; Mitigations: temporary hot key replication and surge rate limits.

## Checklist recap
- Validate routing key against top queries; quantify skew and growth.
- Pick K buckets (≥ 10× planned shards); document remap plan.
- Plan rebalancing and hot‑spot mitigation before launch; test with load models.
