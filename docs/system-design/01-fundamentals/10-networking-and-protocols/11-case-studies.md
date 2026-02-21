---
title: Case Studies
---

# Case Studies

Overview
Real architectures illustrate protocol choices, performance trade‑offs, and operational playbooks in production.

Case Study 1 — Global SaaS API Front Door
- Context: Multi‑region API serving enterprise clients worldwide.
- Design: Anycast IP at edge; TLS 1.3; enable h2+h3 with Alt‑Svc; gateway enforces auth/quotas; internal gRPC with mTLS; DNS weights for region canaries.
- Quantitative: Enabling h3 reduced p95 TTFB 25% for APAC mobile clients (loss ~0.7%), with +5% CPU at edge; adoption gated by fallback health.
- Operations: During provider UDP/443 issue, auto‑fallback to h2 kept error budget burn within SLO.

Case Study 2 — Mobile Notifications with SSE
- Context: Millions of mobile clients need push‑only updates.
- Design: SSE at edge via CDN; token auth on connect; events sourced from Kafka; replay via last‑event‑id.
- Quantitative: Idle memory per conn 12 KB with optimized buffers; 1M concurrent users across 100 nodes ≈ 12 GB baseline.
- Operations: Reconnect storms mitigated with jittered backoff and regional drains.

Case Study 3 — Trading Platform gRPC Microservices
- Context: Low‑latency internal RPCs with strict tail SLOs.
- Design: gRPC over h2; deadlines 50–200ms; retries only for UNAVAILABLE; hedging disabled for writes; mesh enforces mTLS and circuit breakers.
- Quantitative: Switching JSON→protobuf cut p99 by 18% and CPU by 35% under load tests.
- Operations: Canary failures caught via elevated DEADLINE_EXCEEDED; auto‑rollback via gateway.

References
- Public vendor write‑ups: Cloudflare/Google QUIC, Slack WS scale, Netflix/Envoy/Istio posts
