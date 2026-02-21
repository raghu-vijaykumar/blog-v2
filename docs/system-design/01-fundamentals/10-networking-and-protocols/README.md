---
title: Networking & Protocols
---

# Networking & Protocols

Build reliable, efficient networked systems by understanding how name resolution, routing, transport, and application protocols interact in production. This module provides practical guidance on DNS and anycast, TLS and transport choices, HTTP evolution (1.1/2/3), gRPC and realtime channels, proxies and gateways, and day‑2 operations and troubleshooting.

What you will learn
- How DNS, resolvers, and anycast shape global routing and failover
- Transport choices (TCP, TLS, QUIC), path MTU/MSS, and termination patterns
- HTTP/1.1 vs HTTP/2 vs HTTP/3 trade‑offs, multiplexing, and HoL blocking
- Binary RPC (gRPC), streaming modes, deadlines/cancellation, and gateways
- Realtime channels: WebSockets vs SSE vs long‑polling—when to use which
- Proxies, API gateways, egress control, and service mesh TLS/mTLS
- Connection management: keep‑alives, pooling, flow control, timeouts/retries
- Operations: packet loss vs latency, cert issues, MTU blackholes, runbooks

Suggested reading order
1) 01-dns-and-name-resolution.md — mapping names to working endpoints globally
2) 02-ip-routing-anycast-and-egress.md — addressing, routing, anycast, and egress control
3) 03-tls-and-transport-fundamentals.md — TLS handshakes, SNI/ALPN, TCP vs QUIC
4) 04-http-versions-and-performance.md — HTTP/1.1/2/3 and performance in practice
5) 05-grpc-and-binary-rpc.md — binary RPC over h2/h3, streaming, deadlines
6) 06-realtime-protocols-websockets-sse-long-polling.md — realtime delivery options
7) 07-proxies-gateways-termination-and-mesh.md — L4/L7 proxies, gateways, service mesh
8) 08-connection-management-and-timeouts.md — keep‑alives, pooling, budgets, HoL
9) 09-operations-observability-and-troubleshooting.md — tools, dashboards, runbooks
10) 10-selection-guide-and-comparisons.md — pick the right protocol/stack
11) 11-case-studies.md — architectures from the field

Adjacent topics
- Load balancing algorithms and health: ../02-load-balancing/README.md
- Security: TLS, mTLS, authN/Z: ../12-security-and-auth/README.md
- Observability and SLOs: ../11-observability/README.md
- Availability & fault tolerance (timeouts, retries, hedging): ../09-availability-and-fault-tolerance/README.md
- Rate limiting and backpressure: ../08-rate-limiting-and-backpressure/README.md
- Messaging & streaming trade‑offs vs RPC/realtime: ../07-messaging-and-streaming/README.md

References
- RFCs: DNS (1034/1035), HTTP/1.1 (7230+), HTTP/2 (7540), HTTP/3 + QUIC (9114/9000)
- IETF TLS 1.3 (8446), ALPN (7301), SNI (6066)
- Google SRE/Workbook (latency budgets, overload, troubleshooting)
- High Performance Browser Networking (Ilya Grigorik)
- Envoy, NGINX, HAProxy, Istio documentation
