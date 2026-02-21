---
title: Models and Definitions
---

# Models and definitions

Build a precise vocabulary for availability and resilience so designs, SLOs, and incident reports mean the same thing to everyone.

## What / Why / When
- What: Availability is the probability a system can successfully serve requests at a point in time. Fault tolerance is the ability to continue operating despite component failures.
- Why: Clear definitions prevent cargo-cult designs and enable quantitative trade-offs (e.g., N+1 vs quorum write policies, MTTR impact).
- When: Any system with uptime promises, user SLAs, or revenue impact from downtime.

## Core concepts and variants
- Availability (A): A = Uptime / (Uptime + Downtime). Often expressed in “nines” (99.9%, 99.99%).
- Reliability vs Availability: Reliability focuses on failure frequency; availability includes repair time (MTTR). A ≈ MTTF / (MTTF + MTTR).
- MTBF, MTTF, MTTR: Mean time between/failure/repair. Lowering MTTR is often cheaper than raising MTTF.
- Series vs Parallel: In series, availabilities multiply (A_total = Π A_i). In parallel (redundant) components, A_total = 1 − Π(1 − A_i).
- SLA vs SLO vs SLI: SLA is the contract; SLO is the internal target; SLI is the measured metric (e.g., success rate, tail latency).
- Graceful degradation: Maintain core functionality under stress (e.g., hide recommendations, keep checkout working).
- Fault containment: Bulkheads, blast-radius limits, and cell-based architectures to localize incidents.

## Design decisions and trade-offs
- Increase redundancy (N+K) vs reduce repair time (MTTR): Redundancy can be costly; improving automation and rollback often yields better ROI.
- Strict consistency vs availability: Under partitions, CAP implies trade-offs; choose per workload (see Consistency & CAP module).
- Stateful vs stateless tiers: Stateless scales and fails over easily; stateful needs replication, quorum, and fencing.
- Active-active vs active-passive: Active-active reduces RTO but adds complexity (replication conflicts, split-brain risks).

## Algorithms and policies (conceptual)
- Availability math for independent redundant components: A_parallel = 1 − (1 − A)^n.
- Error budget policy: If budget burn rate > threshold, freeze risky releases and prioritize reliability work.
- Rollback/roll-forward policy: Standardize deploy safety (canary, slow start, automated rollback on SLO breach).

## Architecture and components
- Edge: DNS, Anycast, CDN with multiple PoPs; failover mappings.
- Routing: LBs/ingress with health checks, circuit breaking, slow start, connection pools.
- Services: Stateless compute N+K; autoscaling.
- State: Replication factor ≥ 3, quorum reads/writes, fencing on promotion.
- Observability: SLI collection (success rate, latency, saturation), blackbox probes.

## Operational considerations
- SLO definition and ownership; error budget reviews (weekly/monthly).
- Capacity headroom (e.g., 30%) to absorb failover and retries.
- Runbooks for failover, promotion, and dependency outage workarounds.
- Game days and chaos drills to keep procedures fresh.

## Examples
Quantitative example (availability multiplication)
- API depends on auth (99.95%) and database (99.9%) in series; service itself is 99.95%.
- A_total ≈ 0.9995 × 0.9995 × 0.999 = 0.998 ≈ 99.8% (≈17.5 hours/month of potential downtime).
- Action: raise DB availability to 99.95% or reduce MTTR with fast failovers to hit 99.9%+ overall.

Architectural example (graceful degradation)
- If recommendations or personalization service is down, render static content and proceed to checkout.
- Use bulkheads and timeouts so optional calls never block the critical path.

## Edge cases and anti-patterns
- Hidden partial failures: Brownouts with high tail latency that pass “up/down” checks.
- Retry storms: Unbounded client retries amplify incidents; always use budgets and jitter.
- Split-brain: Dual leaders without fencing; causes data corruption.

## Interactions with adjacent topics
- Load balancing health and resilience: ../02-load-balancing/03-health-and-resilience.md
- Replication, quorums, and failover: ../04-replication/README.md and ../05-consistency-and-cap/03-quorums-and-read-policies.md
- Backpressure and rate limiting: ../08-rate-limiting-and-backpressure/README.md

## Production checklist
- Define SLIs and SLOs (success rate, latency percentile) and publish error budgets.
- Enforce per-try and overall timeouts; set retry budgets with jitter.
- Ensure N+1 (or quorum) across fault domains (AZ/zone); test failover.
- Add graceful degradation for non-critical features.
- Create, rehearse, and store runbooks with clear on-call ownership.

## Interview framing checklist
- Define availability vs reliability; compute availability of series vs parallel.
- Discuss N+1 vs quorum trade-offs and MTTR vs MTTF improvements.
- Explain graceful degradation and how to avoid retry storms.

## References
- Google SRE Book/Workbook (SLOs, error budgets)
- Nygard, Release It!
- AWS, Azure, GCP Well-Architected Reliability Pillars
