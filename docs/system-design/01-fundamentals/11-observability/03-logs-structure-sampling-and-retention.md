---
title: Logs — Structure, Sampling, and Retention
---

# Logs — Structure, Sampling, and Retention

Overview
Logs are discrete, structured events for forensic detail, compliance, and workflow automation. Good logs are schematized, correlated to traces, sampled by value, and retained by business need.

What / Why / When
- What: Structured JSON events with stable keys, severity, timestamp, and correlation IDs.
- Why: Root-cause visibility, auditability, and enrichment for automation (dedupe, routing, SOAR).
- When: From day one. Increase structure and sampling discipline with scale.

Core concepts and variants
- Structure: JSON with consistent key names; prefer enums for type-like fields.
- Correlation: include trace_id/span_id, request_id, tenant_id (bounded cardinality only).
- Levels: DEBUG/INFO/WARN/ERROR; keep DEBUG off in prod or route to short retention.
- Sampling: drop-by-default for noisy sources; promote errors/WARN and rare code paths.
- Redaction/tokenization: avoid PII/PHI in logs; hash irreversible IDs where needed.
- Retention tiers: hot (hours–days), warm (days–weeks), cold/archive (months–years, object store).

Design decisions and trade-offs
- Human readability vs. machine parsing: choose structured, machine-first JSON; use viewers for humans.
- Verbosity vs. cost: aggressive sampling saves cost but can hide rare root causes. Use dynamic sampling.
- Centralization vs. locality: central stores ease correlation but add egress cost; edge buffers reduce loss.

Algorithms/policies
- Value-based sampling: sample_rate = f(level, route, user_type, error_code). Errors/WARN = 100%; INFO/DEBUG low.
- PII guardrail: deny-list at source; schema validator blocks deployments introducing disallowed keys.
- Drop noisy labels: normalize path → route_template; cap user_agent variations.

Architecture and components
- Emit structured JSON logs via SDKs. Ship with agents (Fluent Bit/Vector/OTel Collector) → log store (Loki/OpenSearch/Elastic) → search/dashboards.
- Enrich at collector: add resource attrs (service.name, cluster, region), drop fields, sample by value.

Operational considerations
- Backpressure: queue/buffer with disk; drop oldest DEBUG first.
- Multi-tenant: namespace separation, quotas, RBAC on fields.
- Compliance: WORM storage for audit streams; immutability windows; encryption at rest and in transit.

Examples
1) Quantitative — logging cost estimate
   - 10 services, 2k RPS each; INFO emits 1 log/request avg 500 bytes. Raw ≈ 10×2000×0.5 KB = 10 MB/s ≈ 864 GB/day.
   - Apply sampling: keep INFO 10%, ERROR/WARN 100% (assume 1% WARN/ERR). New ≈ (0.1×10 MB/s) + 0.1 MB/s ≈ 1.1 MB/s → ~95 GB/day.

2) Architectural — log schema and pipeline
   - Example event schema (redacted):
```json
{
  "ts": "2026-02-16T11:30:00Z",
  "level": "WARN",
  "service": "checkout",
  "trace_id": "c0ff…",
  "span_id": "a1b2",
  "route": "/orders/{id}",
  "tenant": "small_business",
  "code": "PAYMENT_TIMEOUT",
  "msg": "Upstream gateway timeout",
  "duration_ms": 820,
  "pii": false
}
```
   - Pipeline: app → OTel Collector processors (redact, sample) → Loki/OpenSearch → dashboards/alerts.

Edge cases and anti-patterns
- Free-form strings as labels; stack traces as labels; PII in logs.
- Printing JSON as strings; double-encoding.
- Synchronous logging on hot paths; blocking I/O.

Interactions with adjacent topics
- Security and privacy: ../12-security-and-auth/README.md
- Tracing correlation and exemplars: 04-distributed-tracing-context-propagation-and-baggage.md

Production checklist
- Enforce JSON schema; reject unknown keys in CI.
- Always include trace_id/span_id and route_template.
- Configure value-based sampling and retention tiers.
- Redact PII at source; verify via automated tests.

Interview framing checklist
- Discuss structured vs. unstructured logs; value-based sampling; retention strategies; PII risk.

References
- OTel Logs spec; Grafana Loki; OpenSearch; Elastic Common Schema.
