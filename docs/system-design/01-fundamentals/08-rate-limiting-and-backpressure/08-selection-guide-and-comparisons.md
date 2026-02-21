---
title: Selection Guide and Comparisons
description: Practical decision guide for choosing algorithms, scopes, and enforcement architectures. Compares token/leaky buckets, sliding windows, and GCRA; edge vs service vs centralized enforcement; and fallback strategies.
---

## Overview
This guide helps you quickly choose a policy stack for rate limiting and backpressure given your workload, multi-tenancy model, and downstream constraints.

## What, Why, When (and when-not)
What
- A set of choices—algorithm, scopes, enforcement locus, backpressure policies—that fit common deployment patterns.

Why
- Reduce design churn, align with SLOs, and avoid known anti-patterns under contention.

When
- New services, API gateways, or major refactors of multi-tenant tiers.

When-not
- Niche, extremely low-latency in-process workloads where concurrency-only control suffices.

## Core comparisons
Algorithms
- Fixed window: simplest; boundary bursts; good for coarse quotas or dashboards.
- Sliding window: smoother than fixed; higher state; good for analytics-leaning semantics.
- Token bucket: O(1) state; allows configured bursts; excellent general-purpose edge limiter.
- Leaky bucket: steady drain; queue-centric; use when you want constant egress rate.
- GCRA: O(1) state with precise spacing; excellent at very high QPS with per-key fairness.

Scopes
- Global: easy but risky for noisy neighbors; pair with tenant scopes.
- Per-tenant: primary fairness unit for SaaS.
- Per-endpoint: protect critical paths; weight costly operations with multi-token costs.
- Hierarchical (tenant→endpoint): precise control; slightly more complex operations.

Enforcement locus
- Client-side: fastest feedback; limited trust; best-effort.
- Edge/API GW: high-QPS, coarse fairness; central place for Retry-After and headers.
- Service middleware: fine-grained endpoint/class control; integrate with concurrency and queues.
- Centralized store (Redis/DB): global fairness and quotas with network RTT dependency.
- Decentralized/local-first: ultra-low latency; approximate global consistency.

Backpressure and shedding
- 429 + Retry-After: immediate protection; requires good client behavior.
- Queue-time admission: keeps queue delay within budget; drop early.
- Brownout: degrade optional features to cut downstream load.
- Circuit breaker: stop calls to failing dependencies; allow probes.

## Decision guide (quick paths)
- Public multi-tenant API with tiered plans
  - Algorithm: GCRA or token bucket
  - Scopes: tenant → endpoint (hierarchical), plus global guardrail
  - Enforcement: edge (global/tenant) + service (endpoint); centralized Redis with Lua for global fairness
  - Backpressure: 429 + Retry-After, client SDK with full-jitter and retry budget

- Internal fan-in service with hot endpoints
  - Algorithm: token bucket per endpoint + concurrency caps
  - Scopes: endpoint, optionally tenant if multi-tenant
  - Enforcement: service middleware, local-first; fall back to local-only on store issues
  - Backpressure: queue-time admission; breaker on dependencies; class-based shedding

- High-QPS edge with strict latency SLOs
  - Algorithm: GCRA per tenant at edge (O(1) state), token leasing from Redis
  - Scopes: tenant and IP; endpoint handled in service
  - Enforcement: edge primary; service secondary
  - Backpressure: hard 429 at edge; adaptive token accrual under stress

## Trade-offs matrix (bulleted)
- Accuracy vs latency: centralized > accurate, local-first > low latency
- Burst handling: token bucket best for short bursts; GCRA smoothest spacing
- State cost: sliding > token ≈ GCRA
- Failure impact: centralized introduces single dependency; design fallback
- Multi-region: prefer region-local budgets with spillover rather than cross-region global counters

## Examples
Example A (quantitative): Choosing burst for SLO
- SLO P95 ≤ 200 ms; downstream stable at 3,500 rps with 30% headroom.
- Choose token bucket r = 3,500 rps shared; per-tenant r_t per plan; set b = 2×r_t to absorb ~2 s bursts without breaching tail.

Example B (architectural): Hybrid enforcement
- Envoy at edge enforces tenant/global via Redis Lua token bucket; services apply endpoint-local limits and concurrency caps.
- Store failure triggers local GCRA fallback and reduced token leasing; circuit breakers protect dependencies.

## Edge cases and anti-patterns
- Global-only limits without tenant scoping; synchronized retries without jitter; unlimited queues.

## Interactions with adjacent topics
- [Models & Algorithms](./01-models-and-algorithms.md), [Fairness](./02-fairness-scoping-and-quotas.md), [Distributed Enforcement](./03-distributed-enforcement-and-storage.md)

## Production checklist
- Validate scopes/algorithms against SLO math and downstream capacity.
- Define fallback modes and test game days.
- Publish client guidance and SDK defaults (jitter, budgets, headers).

## Interview framing checklist
- How to design limits for a bursty, multi-tenant public API with paid tiers?
- Edge vs service enforcement—where and why?

## References
- Envoy RLS; Redis Lua patterns; IETF RateLimit headers; AWS backoff & jitter guidance
