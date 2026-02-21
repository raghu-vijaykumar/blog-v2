---
title: Operations, Observability, and Runbooks
description: Metrics, dashboards, alerts, and step-by-step runbooks to operate rate limiting and backpressure safely in production.
---

## Overview
Effective operations require visibility into saturation, fairness, and failure modes, plus clear runbooks to respond quickly without collateral damage.

## What, Why, When (and when-not)
What
- Operational telemetry (metrics/logs/traces), SLOs, alerts, and prescriptive runbooks for limiters, queues, and backpressure.

Why
- Detect noisy neighbors early, prevent retry storms, and keep latency within budgets; shorten MTTR during incidents.

When
- Always on for externally exposed or multi-tenant services; especially where downstreams are costly or rate-limited.

When-not
- Non-critical batch systems where occasional overrun is acceptable and user-facing SLOs do not apply.

## Core metrics and dashboards
- Admissions: allowed/denied by scope (tenant, endpoint), 429 rate, Retry-After histogram.
- Saturation: token deficits, limiter decision latency, queue depth/age, in-flight count, concurrency permits used.
- Downstream health: dependency latency/error rates, breaker state, connection pool usage.
- Fairness: per-tenant utilization vs weight/plan, top-N hot keys, spillover usage.
- Store health (if centralized): Redis/DB p50/p95 latency, error rate, CPU/memory, evictions, connection pool saturation.

## Alerts and thresholds (examples)
- 429_sustained_rate > 5% for 10 minutes (warn), > 15% (page).
- limiter_decision_p95 > 5 ms at edge (warn), > 10 ms (page).
- queue_age_p95 exceeds budget for 5 minutes (page).
- retry_amplification_ratio > 1.2× (page): total_requests / originals.
- store_error_rate > 1% for 5 minutes; failover to fallback mode.

## Runbooks
Brownout and shedding
1) Validate the dependency in distress (DB/cache/API) via latency/error dashboards.
2) Enable brownout for optional features; scale back token accrual by 20–30% for impacted endpoints.
3) Shed non-critical classes; protect health/auth endpoints.

Noisy neighbor containment
1) Identify top tenants by admits/denies and latency impact.
2) Reduce per-tenant r and b by 30–50% temporarily; communicate via headers/webhooks.
3) If global impact persists, isolate to a separate pool or shard.

Limiter store degradation (Redis/DB)
1) Open circuit to central store for checks; switch to local approximate limits (fail closed for critical paths, open for low-risk).
2) Increase token lease duration at edge to reduce store QPS.
3) Add capacity or remediate hot shards (key splitting), then gradually revert.

Retry storm mitigation
1) Increase Retry-After values; push client config to increase backoff cap and jitter.
2) Enforce stricter retry budget (e.g., from 10%→5%).
3) Monitor amplification ratio trending down before restoring defaults.

## Examples
Example A (quantitative): Alert tuning for 429s
- Baseline 429 rate is 1%. Set warn at 5% sustained 10 min; page at 15%. With 100k rps, this prevents paging on short spikes yet catches prolonged overload.

Example B (architectural): Fallback under Redis partition
- Edge enables token leasing (500 tokens/1s) and local GCRA when Redis errors > 5%. Service retains endpoint-local limits. Incident remains contained; over-admit is bounded by lease size until Redis recovers.

## Edge cases and anti-patterns
- Paging on instantaneous 429 spikes causes alert fatigue; use rolling windows and hysteresis.
- Missing per-scope metrics hides noisy neighbors; always dimension metrics by tenant/endpoint.

## Interactions with adjacent topics
- [Distributed Enforcement](./03-distributed-enforcement-and-storage.md): store health and fallback tie directly to operations.
- [Backpressure & Shedding](./04-backpressure-signals-and-load-shedding.md): thresholds feed runbooks for brownout and shedding.

## Production checklist
- Build dashboards for admits/denies, queue depth/age, in-flight, limiter store latency/errors, breaker states.
- Set alert thresholds with hysteresis; test paging paths.
- Document brownout, shedding, and fallback procedures; run game days quarterly.

## Interview framing checklist
- What do you alert on to detect retry storms early? How do you bound amplification?
- How do you operate safely if the centralized limiter store degrades?

## References
- Google SRE practices; Envoy/Redis ops guides; Netflix overload control posts
