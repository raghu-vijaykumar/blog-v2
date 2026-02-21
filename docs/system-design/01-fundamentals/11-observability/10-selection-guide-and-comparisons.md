---
title: Selection Guide and Comparisons
---

# Selection Guide and Comparisons

Overview
Choose a stack that matches your scale, skills, and compliance needs. Mix OSS and managed where it makes sense.

What / Why / When
- What: Comparative guidance for metrics, logs, and traces backends and visualization/alerting tools.
- Why: Avoid lock‑in or underpowered stacks; optimize cost and operability.
- When: Revisit at scale inflection points and compliance changes.

Core options
- Metrics: Prometheus (+ Thanos/Mimir) vs. Managed (Cloud Monitoring, Datadog, New Relic).
- Logs: Loki vs. OpenSearch vs. Elastic vs. vendor platforms.
- Traces: Tempo/Jaeger vs. vendor platforms.
- Dashboards/alerts: Grafana/Alertmanager vs. vendor UIs.

Decision cues
- Team size: small teams prefer managed; larger infra teams can operate OSS economically.
- Workload: high-cardinality or long retention favors managed or carefully tuned OSS at scale.
- Compliance: data residency may force regional OSS; vendors with regional POPs can help.
- Ecosystem: Kubernetes-native favors Prometheus/Grafana/Loki/Tempo.

Examples
1) Quantitative — rough monthly cost comparison
   - OSS: $X infra for 300k samples/s metrics, 100 GB/day logs, 1000 spans/s traces, plus eng time.
   - Vendor: $Y ingestion/retention fees but lower ops toil; watch egress and vendor lock-in.

2) Architectural — hybrid approach
   - Metrics in OSS (Prometheus→Mimir) for cost; traces/logs in managed for speed; export exemplars to managed tracing.

Edge cases and anti-patterns
- Choosing by hype; ignoring run-rate cost; mixing too many stacks.

Production checklist
- Define SLAs/SLOs for the observability platform itself; monitor ingestion lag, query latency, and data loss.

References
- Vendor calculators; Prometheus/Mimir/Thanos; Loki/Tempo/Jaeger docs.
