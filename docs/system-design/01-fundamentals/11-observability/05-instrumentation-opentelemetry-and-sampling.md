---
title: Instrumentation, OpenTelemetry, and Sampling
---

# Instrumentation, OpenTelemetry, and Sampling

Overview
Standardized instrumentation makes telemetry portable across vendors and reduces lock‑in. OpenTelemetry (OTel) provides SDKs, semantic conventions, and the Collector for in-flight processing.

What / Why / When
- What: Auto/manual instrumentation via OTel SDKs; Collector pipelines (receivers → processors → exporters); sampling strategies.
- Why: Consistency, interoperability, and powerful policy control (cardinality, redaction, sampling) without code redeploys.
- When: From the first service; adopt Collector before scale to centralize policy.

Core concepts and variants
- Auto vs. manual instrumentation; semantic conventions for http, db, messaging.
- Resource attributes: service.name, service.version, deployment.environment, region, zone.
- Sampling strategies: head, tail, per-route, adaptive (reservoir), traffic-shedding under overload.
- Collector topology: per-node agents + central gateway; or gateway-only for serverless.

Design decisions and trade-offs
- Agents vs. sidecars vs. daemonsets: choose per platform; agents reduce network hops; gateways centralize policy.
- Head sampling is cheap but noisy on rare issues; tail sampling captures anomalies.
- Exporters: OTLP preferred; use vendor exporters only when necessary.

Algorithms/policies
- Adaptive head sampling: higher sample rate for error-prone routes; lower for healthy ones.
- Attribute processors: drop/relabel noisy labels at Collector; enforce allow-list of attributes.

Architecture and components
- Typical: App SDKs → Node Collector (daemonset) → Gateway Collector → Backends (Metrics/Logs/Traces).
- Gateways run processors for redaction, sampling, and routing to multi-tenant backends.

Operational considerations
- Performance budgets: ≤ 1–2% CPU, minimal allocation churn; batch exporters to cut overhead.
- Versioning: include service.version for canary/rollback analysis.
- Reliability: local queues with disk spillover; backpressure policies to avoid app impact.

Examples
1) Quantitative — overhead planning
   - If each span export is ~300 bytes and avg 10 spans/trace, at 5% head sampling of 10k RPS → 500 tps traces → 5,000 spans/s ≈ 1.5 MB/s. Batch to 50–100 spans per export to reduce syscalls.

2) Architectural — Collector config for tail sampling and redaction
```yaml
receivers:
  otlp:
    protocols: { http: {}, grpc: {} }
processors:
  attributes:
    actions:
      - key: http.target
        action: delete
      - key: user.email
        action: delete
  tailsampling:
    policies:
      - name: errors
        type: status_code
        status_code:
          status_codes: [ERROR]
      - name: slow-traces
        type: latency
        latency:
          threshold_ms: 500
exporters:
  otlphttp: { endpoint: https://tempo.example.com/otlp }
service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [attributes, tailsampling]
      exporters: [otlphttp]
```

Edge cases and anti-patterns
- Double-instrumentation (framework + manual) causing duplicate spans/metrics.
- Per-request baggage inflation; dynamic attribute explosion.

Interactions with adjacent topics
- Cost governance and storage tiers: 08-telemetry-storage-pipeline-and-costs.md
- Security and privacy policies: ../12-security-and-auth/README.md

Production checklist
- Standardize on OTel SDKs and semantic conventions.
- Deploy Collectors with redaction, relabeling, and sampling policies.
- Monitor Collector health and backpressure; set SLOs for telemetry loss.

Interview framing checklist
- Compare head vs. tail/adaptive sampling; describe Collector pipeline design; discuss semantic conventions.

References
- OpenTelemetry SDK/Collector docs; vendor exporter references; Semantic Conventions.
