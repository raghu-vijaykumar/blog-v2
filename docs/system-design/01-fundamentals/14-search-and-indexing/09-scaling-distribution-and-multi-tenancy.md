---
title: Scaling, Distribution, and Multi-Tenancy
---

# Scaling, Distribution, and Multi-Tenancy

## Overview
Search clusters must scale with data volume, query throughput, and tenant isolation requirements. Achieving predictable performance demands careful shard design, resource governance, and lifecycle management.

## Sharding and Replication
- **Shard sizing**: balance index size, query latency, and resource utilization. Target shards sized for 1-2x heap memory with headroom.
- **Primary/replica layout**: set replica count per SLA (read throughput, durability). Avoid synchronous cross-region writes unless necessary.
- **Routing strategies**: hash-based (document IDs), attribute-based (tenant ID), time-based (indices per time bucket), or hybrid (routing with filtering).

## Capacity Planning
- Model QPS, query complexity, and indexing throughput; include peak multipliers.
- Account for merges, refreshes, and background jobs; maintain operational headroom (30-50%).
- Benchmark query latency across hot/cold nodes; use synthetic traffic to validate scaling assumptions.

## Lifecycle Management
- **Tiered storage**: hot/warm/cold nodes, index lifecycle management (ILM) policies for retention, rollover, shrink.
- **Snapshot/restore**: regular snapshots to durable storage; test restore time to meet RTO/RPO.
- **Auto-scaling**: reactive (CPU, heap, queue length) vs. scheduled (traffic patterns). Combine with workload shedding before saturation.

## Multi-Tenancy Models
- **Shared cluster**: multiple tenants share shards; enforce quotas and query isolation via RBAC and rate limiting.
- **Dedicated indices per tenant**: simpler isolation but higher management overhead; consider index templates.
- **Dedicated clusters**: full isolation for high-security or noisy tenants; use automation for provisioning.
- **Hybrid**: pool small tenants; provide dedicated resources for large ones.

## Observability and Guardrails
- Monitor hot shards, search thread pool saturation, cache hit ratios, garbage collection.
- Track tenant-level metrics (QPS, latency, index size) to enforce quotas.
- Implement admission control and circuit breakers to protect shared resources.
- Use canary deployments and staged rollouts for version upgrades.

## Checklist
- [ ] Shard and replica strategy documented with capacity targets.
- [ ] ILM policies defined for retention, rollover, and archiving.
- [ ] Tenant isolation controls with quotas and rate limits implemented.
- [ ] Snapshot/restore procedures validated regularly.
- [ ] Upgrade playbook with canary and rollback steps maintained.
