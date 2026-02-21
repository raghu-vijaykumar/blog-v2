---
title: Rate Limiting & Backpressure Case Studies
description: Real-world style scenarios showing end-to-end design choices, quantitative sizing, and incident outcomes for APIs, streaming consumers, and third‑party integrations.
---

## Overview
These case studies demonstrate how to combine algorithms, scopes, enforcement points, and backpressure to meet SLOs across different workloads.

## Case Study A: Public Multi‑Tenant Payments API
Context
- Public REST API with 50k rps peak, 10k active tenants, paid tiers (Gold/Silver/Bronze). Critical write path (POST /charges), costlier than reads.

Goals
- P95 ≤ 200 ms for critical endpoints. Prevent noisy neighbors. Avoid duplicate charges under retries.

Design
- Algorithm: GCRA at edge for tenant scope; token bucket per endpoint in service.
- Scopes: hierarchical tenant → endpoint; multi-token cost for POST /charges (cost=2), reads cost=1.
- Enforcement: Edge (Envoy RLS + Redis Lua) for tenant/global; service middleware local token buckets for endpoints.
- Backpressure: 429 + Retry-After at edge; queue-time admission and circuit breaker in service.
- Client behavior: SDK with full-jitter exponential backoff; idempotency keys on POST.

Quantitative sizing
- Downstream steady capacity = 3,500 rps with 30% headroom → target 2,450 rps allocated across tenants.
- Gold tenant default r = 300 rps, burst b = 600; Silver r = 100, b = 200; Bronze r = 30, b = 60.
- Endpoint weights: writes consume 2 tokens; reads 1. Tenant monthly quota stored in DB; soft thresholds at 80/90/95%.

Outcomes
- During a traffic spike from a single Gold tenant, edge GCRA spaces requests; service-level endpoint buckets prevent writes from starving reads. 429 rate < 5% and P95 stays within budget.

Operational notes
- Dashboards show per-tenant admits/denies and Retry-After. Game days validate Redis failover with local fallback.

## Case Study B: Streaming Consumers with Backpressure
Context
- Kafka consumer group processing 200k msgs/s with variable record cost (small vs large events). Stateful processing with external DB lookups.

Goals
- Keep processing lag < 5 minutes; protect DB under irregular spikes.

Design
- Throughput shaping: token bucket per partition to cap pull rate; multi-token cost for heavy records based on size.
- Concurrency caps: per-instance semaphore for in-flight processing; queue-time admission with budget from end-to-end SLO.
- Backpressure: pause/resume partition consumption when lag > threshold or DB latency exceeds budget; shed non-critical enrichment steps (brownout).

Quantitative sizing
- Each worker stable at 2,000 msgs/s with 100 ms P95 processing → permits per worker ≈ λ×W = 2000×0.1 = 200 in-flight.
- When DB P95 doubles from 30→60 ms, reduce token accrual 25% and pause lowest-priority partitions first.

Outcomes
- Lag held under 3 minutes during burst; no DB timeouts. Optional enrichments disabled during brownout, re-enabled after recovery.

Operational notes
- Alerts on lag growth, queue age, and DB P95; runbook automates pause/resume and token rate adaptation.

## Case Study C: Third‑Party API Integration
Context
- Service calls an external partner API limited to 1,000 rps per account; P95 external latency 150–400 ms and occasional 429s.

Goals
- Avoid exceeding partner limits; deliver best-effort user experience without cascading retries.

Design
- Algorithm: GCRA token accrual aligned to partner’s published limits.
- Concurrency cap: 200 in-flight calls to bound threads and connection pools.
- Backpressure: 429 handling with Retry-After; local queue-time admission = 100 ms budget; circuit breaker for partner errors.
- Caching: cache idempotent GETs for 30–60s to cut partner load.

Quantitative sizing
- With partner limit 1,000 rps, set r = 900 rps (10% safety), burst b = 1s = 900 tokens.
- With P95=300 ms, permits N ≈ λ×W = 900×0.3 = 270; choose 200 to preserve headroom.

Outcomes
- Exceeding traffic yields controlled 429s locally; clients back off with jitter. Partner SLA maintained; user-visible latency degrades gracefully.

Operational notes
- Dashboards track partner 429s, Retry-After adherence, cache hit rate; runbook includes fail-open mode for low-risk endpoints during partner incidents.

## Edge cases and anti-patterns observed
- Synchronizing retries without jitter caused sawtooth traffic; fixed by SDK update to full jitter.
- Global-only limits allowed a few tenants to dominate; adding tenant scopes resolved starvation.

## Interactions with adjacent topics
- [Retries & Idempotency](./05-retries-idempotency-and-client-behavior.md) ensured safe POST semantics in payments.
- [Concurrency & Queues](./06-concurrency-limits-queues-and-admission-control.md) bounded in-flight work in streaming and partner calls.

## Production checklist (recap for case deployments)
- Validate algorithm/scopes vs SLO math; include tenant and endpoint scopes where applicable.
- Set client SDK defaults: jittered backoff, limited retries, idempotency keys.
- Define fallback for limiter store and partner errors; test with game days.

## References
- IETF RateLimit headers; Envoy RLS; Kafka consumer pause/resume patterns; AWS backoff & jitter; partner API rate limiting docs
