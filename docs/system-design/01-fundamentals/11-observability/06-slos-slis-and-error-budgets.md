---
title: SLOs, SLIs, and Error Budgets
---

# SLOs, SLIs, and Error Budgets

Overview
Service-level objectives align engineering with user outcomes. SLIs measure what users feel; SLOs set targets; error budgets enable safe velocity.

What / Why / When
- What: SLIs (availability, latency, quality), SLO (target over window), error budget (1 − SLO). Burn-rate alerts trigger action.
- Why: Balance reliability with delivery; reduce toil; make paging objective.
- When: Before GA; refine with production telemetry.

Core concepts and variants
- Availability SLI: success_ratio = good_events / total_events.
- Latency SLI: proportion of requests under threshold T (e.g., 300 ms).
- Quality SLI: correctness (e.g., HTTP 2xx with domain checks), freshness (data age), coverage.
- Windows: rolling (28d) and sub-windows (1h, 6h) for burn-rate alerts.

Design decisions and trade-offs
- Composite SLIs: AND multiple conditions (e.g., status=2xx AND latency `<300ms`).
- Per-customer SLOs: expensive; prefer global SLOs + VIP cohorts.
- Budget policy: freeze deploys on fast burn; error-budget spending priorities.

Algorithms/policies
- Burn-rate = error_rate × SLO_window / short_window. Multi-window examples (Google SRE):
  - 2% of monthly budget in 1 hour (page) and 5% in 6 hours (page), warning at slower burns.
- PromQL availability (good/total) and latency proportion via histogram_quantile.

Operational considerations
- Event definitions must be stable; use route_template not raw path.
- Data gaps: treat as unknown, not success; ensure probe coverage.
- Dashboards show SLI, SLO target, error budget remaining, and burn-rate.

Examples
1) Quantitative — 99.9% monthly availability
   - 30 days ≈ 43,200 minutes; budget = 0.1% = 43.2 minutes of error.
   - Fast-burn page if burn-rate > 14.4 (consuming 2% of monthly budget in 1h) and slow-burn page if > 6 in 6h.

2) Architectural — SLI computation flow
   - Ingest http_requests_total and duration histograms; compute good = `status<500` AND `duration<300ms`. Recording rules publish sli_availability and sli_latency_proportion; dashboards and alerts consume these.

Snippets
- PromQL (availability SLI):
  - `sum(rate(http_requests_total{status!~"5.."}[5m])) / sum(rate(http_requests_total[5m]))`
- PromQL (latency SLI at 300 ms):
  - `histogram_quantile(0.99, sum by (le) (rate(http_server_duration_seconds_bucket[5m]))) < 0.3`
- PromQL (burn-rate):
  - 1 - (good/total) divided by error_budget_per_request; see SRE workbook patterns.

Edge cases and anti-patterns
- Using averages for latency SLIs; mixing client/server errors; counting redirects as success without domain checks.

Interactions with adjacent topics
- Error budgets in availability module: ../09-availability-and-fault-tolerance/07-operations-observability-error-budgets-and-chaos.md

Production checklist
- Define SLIs from user journeys; publish recording rules; set multi-window alerts; agree budget policy with product.

Interview framing checklist
- Explain SLI/SLO/budget; compute budgets; design multi-window alerts; trade-offs for per-tenant SLOs.

References
- Google SRE Book/Workbook; Prometheus recording rules; vendor SLO tooling.
