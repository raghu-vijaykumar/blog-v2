---
title: Fairness, Scoping, and Quotas
description: Designing per-tenant fairness and quotas across endpoints and tiers with hierarchical scoping, weighted shares, and isolation to prevent noisy-neighbor effects.
---

## Overview
Fairness ensures no single tenant, user, or endpoint monopolizes shared capacity. Scoping chooses the keys to which limits apply (tenant, endpoint, IP), and quotas enforce longer-term consumption budgets (hour/day/month) in addition to per-second rates.

## What, Why, When (and when-not)
What
- Policies that carve shared capacity into per-key slices: per-tenant, per-endpoint, per-IP, or hierarchical combinations.
- Quotas: finite allocations over longer windows (e.g., 1M requests/month) with replenishment cadence.

Why
- Prevent noisy-neighbor starvation, protect critical APIs, align usage with plans/SLOs, and enable predictable multi-tenant scaling.

When
- Any externally exposed API or multi-tenant internal service; when endpoints have different costs or criticality.

When-not
- Single-tenant or batch-only systems where simple global caps suffice and fairness adds unnecessary complexity.

## Core concepts and variants
- Scopes/keys: tenant_id, user_id, api_key, IP/CIDR, endpoint/method, region. Combine as composite keys (tenant+endpoint) for precision.
- Hierarchical limits: global → tenant → endpoint. Child admits only if both parent and child limits allow.
- Weighted fairness: assign weights per tier (gold/silver/bronze) to proportionally share limited capacity.
- Isolation pools: dedicate slices of capacity to classes (e.g., background jobs vs interactive) to avoid mutual impact.
- Quotas vs rates: rates protect instantaneous load; quotas enforce aggregate consumption. Use both.

## Design decisions and trade-offs
- Granularity vs state size: finer scopes increase key cardinality and storage; coarser scopes may under-protect critical endpoints.
- Hierarchies vs flat policies: hierarchies better encode business rules but require careful ordering and atomicity across levels.
- Strict vs elastic shares: strict reservations avoid contention but can waste capacity; elastic shares boost utilization but risk burst interference.

## Algorithms and policies (conceptual)
- Hierarchical token bucket: parent bucket gates child buckets; a request decrements both.
- Weighted fair sharing: dynamically allocate per-tenant rates r_t = (w_t / Σw) × R_available; recompute on membership changes.
- Quota enforcement: event counters over rolling month/day windows with soft warnings near thresholds and hard denies past limit.

Pseudocode: hierarchical admit (≤ 25 lines)
```pseudo
function admit_hier(now, tenantKey, endpointKey):
  if !tenantBucket.admit(now):
    return DENY_TENANT
  if !endpointBucket[endpointKey].admit(now):
    tenantBucket.undo()  # optional if atomic: charge only on success
    return DENY_ENDPOINT
  return ALLOW
```

## Architecture and components
- Policy store: plans, weights, and per-scope limits with hot reload.
- Enforcement: edge gate for tenant/global scopes; service-local for endpoint scopes to reduce hot central writes.
- Quota counter store: durable counters with idempotency and backfill tolerance (e.g., database or strongly consistent KV).

## Operational considerations
- Overprovision headroom per tier; prioritize interactive traffic during brownouts.
- Enforce minimums for critical endpoints within a tenant to prevent self-starvation (e.g., auth/health bypasses).
- For quotas, implement soft thresholds (80/90/95%) with notifications before hard stop.

## Examples
Example A (quantitative): Weighted sharing under contention
- Total safe capacity R = 10k rps. Tenants A/B/C with weights 5/3/2.
- Shares: 5k/3k/2k rps. If C uses only 500 rps, elastic sharing can reallocate 1.5k across A/B; strict shares would leave it idle.

Example B (architectural): Tenant→endpoint hierarchy
- Edge enforces tenant-wide token bucket. Service layer enforces per-endpoint buckets. A request to POST /orders decrements both; GET /health bypasses endpoint limits.

## Edge cases and anti-patterns
- Only global cap without tenant isolation causes noisy-neighbor starvation.
- Charging 5xx responses against quotas can punish tenants during platform incidents; prefer excluding 5xx from quotas but include in rate limits only if necessary.

## Interactions with adjacent topics
- [Models and Algorithms](./01-models-and-algorithms.md): choose the algorithm per scope.
- [Backpressure & Shedding](./04-backpressure-signals-and-load-shedding.md): brownouts and class-based shedding interplay with fairness pools.

## Production checklist
- Define scopes and hierarchy; ensure atomic charge across levels or compensating undo.
- Establish weights per plan and document elastic vs strict behavior.
- Implement soft/hard quota thresholds and notifications.

## Interview framing checklist
- How to prevent a single endpoint from starving others within the same tenant?
- How to split limited capacity fairly among dynamic tenants?

## References
- Weighted fair queuing and DRR literature; API pricing/quota best practices (Stripe/GitHub)
