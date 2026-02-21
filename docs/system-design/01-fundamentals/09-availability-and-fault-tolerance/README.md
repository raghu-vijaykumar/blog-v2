---
title: Availability & Fault Tolerance
---

# Availability & Fault Tolerance

Build services that stay up, degrade gracefully, and recover quickly when the inevitable happens. This module covers core reliability models, failure taxonomies, protective client-side and server-side controls, and operational practices that keep systems resilient in production.

What you will learn
- Availability math, redundancy patterns (N+1, N+2, quorum), and graceful degradation
- Failure modes: brownouts vs. blackouts, partitions, retries, and overload dynamics
- Protective controls: timeouts, retries with budgets, circuit breakers, hedging, bulkheads
- Failover and disaster recovery across zones/regions; ties to replication and data durability
- Operations: SLOs and error budgets, chaos testing, incident playbooks, and runbooks

Suggested reading order
1) 01-models-and-definitions.md — the mental model and terminology
2) 02-failure-modes-and-taxonomy.md — what actually goes wrong in prod
3) 03-redundancy-n-plus-k-and-quorums.md — how we add resilience
4) 04-timeouts-retries-circuit-breakers-and-hedging.md — protective controls
5) 05-bulkheads-graceful-degradation-and-slos.md — contain blast radius and degrade well
6) 06-failover-promotion-and-dr.md — staying up across failures and disasters
7) 07-operations-observability-error-budgets-and-chaos.md — run it in prod
8) 08-selection-guide-and-comparisons.md — pick the right mix for your system
9) 09-case-studies.md — learn from real architectures

Adjacent topics
- Load balancing health and resilience: ../02-load-balancing/03-health-and-resilience.md
- Replication and failover details: ../04-replication/README.md
- Consistency and CAP trade-offs: ../05-consistency-and-cap/README.md
- Messaging and retries/DLQs: ../07-messaging-and-streaming/README.md
- Rate limiting and backpressure: ../08-rate-limiting-and-backpressure/README.md

References
- Google SRE Workbook and Principles (SLOs, error budgets)
- Nygard, Release It! (circuit breakers, bulkheads, stability patterns)
- AWS Well-Architected Reliability Pillar
- Netflix Chaos Engineering posts
