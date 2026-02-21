---
title: Alerting, Dashboards, and Runbooks
---

# Alerting, Dashboards, and Runbooks

Overview
Page people only for user-impacting issues; everything else creates tickets. Dashboards should answer a question quickly; runbooks encode repeatable fixes.

What / Why / When
- What: Paging policies, burn-rate alerts, golden-signal dashboards, and runbooks tied to alerts.
- Why: Reduce alert fatigue, speed diagnosis, and improve consistency of incident response.
- When: Before on-call starts; iterate after each incident.

Core concepts and variants
- Paging vs. ticketing: severity thresholds; dashboards for self-serve diagnosis.
- Golden Signals dashboard: latency, traffic, errors, saturation with drill-downs.
- Multi-window burn-rate alerts: fast/slow windows to catch both spikes and smoldering issues.
- Ownership: each alert has an owner, severity, and runbook link.

Design decisions and trade-offs
- Few, meaningful pages vs. many noisy ones; prefer SLO burn-rates over infrastructure-only alerts.
- Dashboard layout: top-level SLI/SLO; then RED/USE per dependency; trace exemplar panel.

Operational considerations
- Game days and runbook tests; post-incident reviews improve alert rules.
- On-call rotation health: page volume SLO; silence windows for maintenance.

Examples
1) Quantitative — paging SLO
   - Target ≤ 2 pages/shift engineer. If exceed for 2 consecutive weeks, invest in alert hygiene/refactors.

2) Architectural — dashboard composition
   - Row 1: SLI targets, error budget remaining, burn-rate.
   - Row 2: RED metrics by route; panel links to traces via exemplars.
   - Row 3: Resource USE (CPU, memory, queue depth) and dependency health.

Snippets
- Alertmanager (burn-rate sketch):
```yaml
groups:
  - name: slo-burn
    rules:
    - alert: FastBurn
      expr: slo_error_ratio:rate1h > 14.4
      for: 5m
      labels: { severity: page }
      annotations: { runbook: https://runbooks.example.com/slo-burn }
    - alert: SlowBurn
      expr: slo_error_ratio:rate6h > 6
      for: 15m
      labels: { severity: page }
```

Runbook template
- Summary, affected services, detection, immediate actions, diagnosis tree, mitigations, verification, rollback, follow-ups.

Edge cases and anti-patterns
- Alerting on every micro-metric; no runbooks; paging on non-actionable warnings.

Interactions with adjacent topics
- Availability/error budgets: ../09-availability-and-fault-tolerance/README.md

Production checklist
- Each page has owner, severity, runbook; dashboards show SLI first; alerts tested in game days.

Interview framing checklist
- Explain burn-rate alerts and golden-signal dashboards; discuss alert hygiene process.

References
- Google SRE Workbook; Grafana/Alertmanager docs; Practical runbooks guides.
