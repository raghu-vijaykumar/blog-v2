---
title: Distributed Tracing, Context Propagation, and Baggage
---

# Distributed Tracing, Context Propagation, and Baggage

Overview
Tracing connects spans into causal graphs across services, queues, and databases to explain where time is spent and why failures occur.

What / Why / When
- What: Spans with attributes, events, status, and links; W3C Trace Context for propagation; optional baggage for small attributes.
- Why: Pinpoint latency and errors across boundaries; accelerate MTTR; enable exemplar pivots from metrics.
- When: As soon as you have more than one hop (service, job, or external dependency).

Core concepts and variants
- Trace Context: traceparent, tracestate headers. Must pass through proxies/gateways.
- Spans: name, kind (server/client/internal/producer/consumer), start/end, attributes, status.
- Links: connect spans across fan-out/fan-in or async boundaries.
- Baggage: tiny, low-cardinality key/values that travel with context (e.g., tenant=tier).
- Sampling: head vs. tail; route-aware or error-aware.

Design decisions and trade-offs
- Span naming: verb + resource (GET /orders/{id}). Avoid raw paths.
- Attribute selection: semantic conventions (http.*, db.*, messaging.*). Avoid high-cardinality values.
- Baggage limits: ≤ a few keys; use metrics/labels for aggregates and logs for details.
- Tail sampling requires central processors; higher fidelity on anomalies at added complexity.

Algorithms/policies
- Span status: set ERROR on 5xx/exception; add event with error.type/message/stack (redacted as needed).
- Tail sampling policy: keep all traces with ERROR or p99 latency ≥ threshold; sample 1% of others.

Architecture and components
- OTel SDKs auto-instrument HTTP/gRPC/SQL; manual spans for business steps.
- OTel Collector tail-sampling processor; export to Tempo/Jaeger/Vendor.
- Gateways and message brokers must propagate trace headers/attributes.

Operational considerations
- Overhead: target `<1–2%` CPU; cap event counts per span; limit attributes per span.
- Privacy: do not place PII in attributes/baggage; prefer IDs and lookups.
- Retention: shorter than logs; keep exemplars to pivot from metrics historically.

Examples
1) Quantitative — head vs. tail sampling volume
   - 20k RPS, average 10 spans/trace.
   - Head sample 5% → 1k tps traces, 10k spans/s.
   - Tail sample errors+slow (2%) at 100%, others 1% → ≈ (400 tps ×10) + (196 tps ×10) ≈ 5,960 spans/s. Better fidelity on issues, lower cost than 5% head in this mix.

2) Architectural — async messaging with links
   - Service A publishes to topic; Service B consumes later. Use a PRODUCER span in A, a CONSUMER span in B, connect with a link. Retain correlation through message headers carrying trace context.

Edge cases and anti-patterns
- Dropping trace headers at gateways/load balancers.
- Putting user_id/email into baggage; unbounded baggage growth.
- Excessive span nesting; turning logs into trace attributes.

Interactions with adjacent topics
- Retries/hedging create duplicate spans; dedupe in analysis: ../09-availability-and-fault-tolerance/04-timeouts-retries-circuit-breakers-and-hedging.md
- Proxies/mesh considerations: ../10-networking-and-protocols/07-proxies-gateways-termination-and-mesh.md

Production checklist
- Enforce W3C Trace Context through all hops (gateways, MQ, batch).
- Configure tail sampling with error/latency policies.
- Add exemplars from latency histograms to traces.
- Document span names and semantic conventions.

Interview framing checklist
- Explain head vs. tail sampling; span kinds; links for async; baggage constraints.

References
- OpenTelemetry Traces; W3C Trace Context; Jaeger/Tempo docs; Semantic Conventions.
