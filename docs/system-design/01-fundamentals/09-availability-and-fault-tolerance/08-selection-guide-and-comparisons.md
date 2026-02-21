---
title: Selection Guide and Comparisons
---

# Selection guide and comparisons

Choose the right availability patterns per tier, with pragmatic defaults and trade-off highlights.

## What / Why / When
- What: Decision guidance for redundancy, quorums, client controls, failover scope, and operational posture.
- Why: Over-engineering reliability wastes budget; under-engineering burns error budgets. Match patterns to risk and workload.
- When: Architecture reviews, readiness for launch, and post-incident hardening.

## Core choices and defaults
- Compute redundancy (stateless)
  - Default: N+1 per AZ with 25–30% headroom; zone-aware routing; autoscaling; slow start.
  - Consider N+2 or cell-based isolation when: high criticality, spiky workloads, or noisy neighbors.
- Data replication & quorums (stateful)
  - Default: RF=3 across AZs; W=2, R=1 for write safety + read latency; fencing on promotion.
  - Prefer majority writes (W>RF/2) for primary systems of record; relax to W=1 (async followers) only if RPO>0 is acceptable.
- Client controls
  - Default: per-try timeout + overall timeout; ≤1 retry with exponential backoff + jitter; retry budget ≤10%; circuit breaker enabled.
  - Hedge only for latency-sensitive reads and only beyond p95 with tight tokens.
- Failover strategy
  - Default: AZ-level resilience, proven runbooks; warm standby for regional with tested RTO.
  - Consider active-active multi-region when: global low-latency and near-zero RTO are required, and conflict resolution is feasible.
- Degradation and bulkheads
  - Default: Identify optional features and define degraded modes; per-dependency concurrency pools with caps.
  - Add cell isolation for multi-tenant or high-blast-radius services.
- Operations
  - Default: 99.9% SLOs on critical paths; dual-window burn-rate paging; synthetic probes for key journeys; quarterly chaos drills.

## Design decisions and trade-offs
- Cost vs availability: Each extra nine can cost an order of magnitude more (extra regions, stronger quorums, larger headroom).
- Latency vs safety: Majority writes add latency; accept when correctness is paramount (orders, money). For reads, leverage caches and read replicas.
- Complexity vs RTO/RPO: Active-active lowers RTO but adds data conflict and operational complexity.
- Degrade vs drop: Degrading maintains UX but can mask systemic problems; ensure robust telemetry and clear user messaging.

## Comparisons (when to choose what)
- N+1 vs quorum-based availability
  - N+1 (stateless): Simple, cheap, fast failover; doesn’t protect state.
  - Quorum (stateful): Necessary for consistency and availability of writes; requires careful placement and fencing.
- Retries vs hedging
  - Retries: Good for transient errors; risk of overload—use budgets.
  - Hedging: Targets long-tail latency; use sparingly with tokens and cancelation.
- Active-passive vs active-active
  - Active-passive: Lower cost/complexity; RTO minutes; possible RPO>0 depending on replication.
  - Active-active: Near-zero RTO; higher cost and complexity; pay attention to conflict resolution and CAP.
- DNS/GSLB vs L7 failover
  - DNS/GSLB: Coarse, cacheable, global; slower convergence; good for region-level.
  - L7 failover: Fast, granular, per-endpoint; requires robust global control-plane.

## Examples
Quantitative example (budget vs availability)
- Service target 99.9% (43m/month). Moving to 99.99% (4m20s/month) cuts allowable downtime by ~10×. To hit 99.99%, you likely need active-active or very fast, automated AZ failover and `MTTR<1m` per incident—budget for extra regions, automation, and on-call maturity.

Architectural example (tiered approach for checkout)
- Stateless APIs: N+1 per AZ, 30% headroom, zone-aware LB.
- Payments DB: RF=3, majority writes, sync cross-AZ; fenced promotion.
- Recommendations: Optional, cached fallback with 100ms timeout and no retries.
- Regional DR: Warm standby with tested 5-minute RTO.

## Decision checklist (apply per tier)
- Criticality: What SLO and business impact? (choose 99.9 vs 99.99 accordingly)
- Workload: Read-heavy vs write-heavy? Latency sensitivity? Burstiness?
- State: Needed consistency level? Tolerable RPO? Promotion safety (fencing)?
- Failure domains: AZ vs region—what do we design to survive?
- Client controls: Timeouts, retries, breaker, and budgets configured?
- Degradation plan: What becomes optional and how is it disabled?
- Ops readiness: Runbooks, chaos drills, and budget burn alerts in place?

## Interactions with adjacent topics
- Replication and quorums: ../05-consistency-and-cap/03-quorums-and-read-policies.md
- Load balancing resilience: ../02-load-balancing/03-health-and-resilience.md
- Backpressure and load shedding: ../08-rate-limiting-and-backpressure/04-backpressure-signals-and-load-shedding.md
- Observability and SLOs: ../11-observability/README.md

## Production checklist
- Pick defaults above; document exceptions with rationale and SLO alignment.
- Size headroom for AZ failure plus retry amplification.
- Verify R+W>RF for state; implement fencing; test promotion.
- Enable retry budgets and per-route timeouts; log breaker state.
- Define degraded modes and verify user messaging.

## Interview framing checklist
- Given a checkout system, propose availability patterns per tier and justify with SLO and workload.
- Trade off active-passive vs active-active for a global API.
- Choose between retries and hedging for a p99-latency-sensitive read.

## References
- Google SRE (availability, SLOs)
- AWS/Azure/GCP Well-Architected Reliability Pillars
- Nygard, Release It!
