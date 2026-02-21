---
title: Operations, Observability, and Troubleshooting
---

# Operations, Observability, and Troubleshooting

Overview
Day‑2 operations turn telemetry into faster diagnosis and safer changes. Build consistent workflows for triage, drill‑down, and mitigation.

What / Why / When
- What: Incident triage flows, pivoting across signals, synthetic checks, chaos tests, and post-incident learning.
- Why: Lower MTTR, fewer regressions, and more confident releases.
- When: Always on; refine with each incident.

Core concepts and variants
- Triage loop: page → identify user impact → check SLOs → isolate blast radius → mitigate → root cause.
- Pivoting: SLO widget → RED histogram → exemplar trace → correlated logs.
- Synthetic checks: blackbox probes on critical journeys.
- Change correlation: overlay deploy markers; compare canary vs. baseline.

Design decisions and trade-offs
- Wide vs. deep dashboards; start wide (SLIs), drill deep via links.
- Detect vs. prevent: combine SLO alerts with change blocking (error-budget policy).

Operational considerations
- On-call ergonomics: quick links, saved queries, and keyboard-driven UIs.
- Game days and runbooks validation; chaos engineering to reveal gaps.

Examples
1) Quantitative — MTTR improvement goal
   - Baseline MTTR 45 minutes; target 20. Invest in exemplar wiring, burn-rate alerting, and single-click trace pivots.

2) Architectural — triage flow
   - Alert fires (FastBurn). On-call opens SLI dashboard → sees p99 spike. Click exemplar trace → DB saturation on shard 3. Mitigate: enable read-only mode for non-critical routes; raise pool size temporarily; initiate cache warming.

Edge cases and anti-patterns
- Dashboard sprawl; no ownership; stale runbooks; manual, error-prone mitigations.

Interactions with adjacent topics
- DR and failover: ../09-availability-and-fault-tolerance/06-failover-promotion-and-dr.md
- Rate limiting/backpressure during incidents: ../08-rate-limiting-and-backpressure/README.md

Production checklist
- Ensure every page links to triage dashboard; exemplar pivots work; runbooks current and tested.

Interview framing checklist
- Walk a triage scenario; show how to pivot between metrics, traces, logs; discuss incident review improvements.

References
- Google SRE practices; Incident.io/PagerDuty runbooks; Chaos Engineering literature.
