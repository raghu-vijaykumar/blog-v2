---
title: Case Studies
---

# Case Studies

Overview
Selected real-world style scenarios showing how observability improved reliability, performance, and iteration speed. Each highlights signals used, triage flow, and concrete outcomes.

Case study 1 — E‑commerce checkout latency spikes
- Context: 5 microservices (API GW → cart → pricing → payments → orders); intermittent p99 spikes during sales events.
- Signals and triage: SLO burn-rate page fired; dashboard showed RED spike on POST /checkout. Exemplar linked to a trace with repeated upstream timeouts to the payment gateway.
- Root cause: mis-tuned client retry + long timeout compounded queueing; occasional gateway brownouts amplified tail latency.
- Fixes: tightened client timeouts, added circuit breaker and hedging for idempotent calls, separated critical and non-critical dependency pools, introduced per-route histograms aligned to SLO.
- Outcome: p99 down 40%, error budget burn stabilized; pages/shift dropped below target.

Case study 2 — Streaming platform buffer underruns
- Context: Media edge + origin; customers reported stutter under regional load.
- Signals and triage: USE metrics showed disk saturation at origin; traces linked CDN edge misses to origin fetches; logs exposed a path segment causing cache fragmentation.
- Root cause: mismatched cache key policy + short TTLs causing low hit rate and origin overload.
- Fixes: unified cache key normalization, increased TTLs for static segments, prefetch on trending assets, queue-depth alerts.
- Outcome: origin write IOPS down 55%, underruns reduced 60%, user complaints near zero.

Case study 3 — Fintech ledger correctness
- Context: Event-driven pipeline (orders → settlement → ledger). Rare mismatches surfaced weekly.
- Signals and triage: Quality SLI tracked reconciliation failures; traces stitched async steps via span links; structured logs carried correlation IDs.
- Root cause: duplicate delivery + non-idempotent consumer on a side path; missing fencing on retry.
- Fixes: idempotency keys, transactional outbox, DLQ surfacing in dashboards, lineage logs with redaction.
- Outcome: correctness SLI improved to 99.99%; mean time between incidents increased 10×.

Case study 4 — SaaS multi-tenant noisy neighbor
- Context: Shared DB and cache tiers; one tenant’s burst degraded others.
- Signals and triage: Per-tenant RED and USE panels highlighted saturation correlated with one tenant_id (low-cardinality cohort). Tail-sampled traces showed long mutex waits.
- Root cause: unbounded concurrent report generation; cache stampede on a hot key.
- Fixes: per-tenant concurrency caps, request budgets, token-bucket cache refresh, negative-caching for misses.
- Outcome: SLO compliance for all tenants; platform cost steady with predictable budgets.

Lessons learned
- Exemplars accelerate root cause by pivoting from SLO/RED panels to concrete traces.
- Tail sampling keeps the “interesting” traces (errors/p99) at reasonable cost.
- Per-route histograms and stable labels prevent cardinality explosions while preserving fidelity.
- Error budgets protect focus by gating change velocity during incidents.

Interactions and references
- Related: 02-metrics-histograms-and-cardinality.md, 04-distributed-tracing-context-propagation-and-baggage.md, 06-slos-slis-and-error-budgets.md.
- References: Google SRE Book/Workbook; Honeycomb/Lightstep/Datadog case write-ups; Netflix/DoorDash engineering blogs on tail sampling and exemplars; Nygard’s Release It!.
