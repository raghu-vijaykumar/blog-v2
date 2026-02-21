---
title: Selection Guide and Comparisons
---

# Selection Guide and Comparisons

Overview
Choose protocols and components based on network conditions, client capabilities, security posture, and operational maturity. This guide summarizes defaults and when to deviate.

What / Why / When
- What: A compact decision aid for DNS steering, transport, HTTP versions, RPC, and realtime.
- Why: Reduce analysis paralysis; adopt battle‑tested defaults.
- When: New services, rewrites, and incident‑driven improvements.

Core concepts and variants
- Public APIs: h2 default, enable h3 with fallback; REST+JSON externally, gRPC internally.
- Internal mesh: mTLS, gRPC for service calls; HTTP/1.1 allowed for legacy.
- Realtime: WebSockets for bidirectional; SSE for push‑only; long‑poll as fallback.
- Routing: DNS weights for coarse shifts; L7 LBs for precise control; anycast for edge.

Design decisions and trade-offs
- h3 benefits vs middlebox risk; gRPC efficiency vs browser compatibility; DNS steering speed vs lack of instantaneous failover.

Algorithms/policies (conceptual)
Decision pseudo‑matrix (simplified):
```
if client_is_browser:
  api = REST over h2 (+h3)
  realtime = WS (bidirectional) or SSE (server->client)
else:
  api = gRPC over h2 (+h3)
  if lossy/mobile: prefer h3
```

Architecture and components
- Combine: Anycast edge → L7 gateway → mesh (mTLS, gRPC) → services → egress with allowlists.

Examples
1) Quantitative — Header compression impact
- Migrating from h1 to h2/h3 with HPACK/QPACK reduces header bytes by ~60–90% for chatty APIs with large cookie/auth headers, saving tens of Mbps at scale and improving TTFB.

2) Architectural — Greenfield service defaults
- Boilerplate: Edge terminates TLS 1.3; enable h2+h3; REST at edge; internal gRPC; retries with budgets; DNS weights for regional shifts; anycast for edge IP.

Edge cases and anti‑patterns
- Picking WS for simple server push; exposing gRPC directly to browsers without gRPC‑Web; using DNS alone for hot failover needs.

Interactions with adjacent topics
- Availability, Security, Observability: Ensure policies and dashboards exist before enabling new protocols.

Production checklist
- [ ] Documented defaults per service type (public API, internal RPC, realtime)
- [ ] Fallbacks tested (h3→h2→h1)
- [ ] Capacity and budgets validated for retries/hedging

Interview framing checklist
- How would you phase‑in HTTP/3 safely for a large API?
- When should a team choose SSE over WebSockets?

References
- Browser vendor guidance, CDN/edge provider best practices
