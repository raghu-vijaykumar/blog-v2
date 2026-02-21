---
title: Foundations and Mental Models
---

# Foundations and mental models

This module frames what load balancing solves, where it lives in the stack, and the primary trade-offs you will make.

## What load balancing solves
- Spread traffic across instances to maximize throughput and minimize tail latency.
- Hide failures and maintenance by routing around unhealthy backends.
- Provide a single entry point (or a few) for security, observability, and policy.

## Client-side vs server-side
- Client-side: the caller chooses a backend using a library/sidecar (e.g., gRPC pick_first/round_robin, Envoy sidecar). Pros: fewer central bottlenecks, per-request telemetry, resilience to LB outages. Cons: harder to centrally manage, requires service discovery on clients.
- Server-side: a centralized reverse proxy or L4/L7 LB selects the backend (NGINX/HAProxy/Envoy, cloud LBs). Pros: centralized policy, TLS termination, WAF, simpler clients. Cons: needs HA for the LB tier itself.

## L4 vs L7
- L4 (TCP/UDP): fast and simple, balances at connection/flow level; opaque to HTTP/gRPC. Great for databases, game servers, MQTT.
- L7 (HTTP/gRPC): application-aware routing by host/path/headers; balances per request/stream with retries, timeouts, and fine-grained policy.

## DNS and Anycast
- DNS load balancing: multiple A/AAAA records or health-checked DNS. Coarse control, cached by resolvers; use modest TTLs. Works well for region selection and basic failover.
- Anycast: same IP advertised from multiple PoPs; BGP selects the topologically closest. Excellent for global edge points (CDN, DoS absorption) with L7 proxies behind.

## Topologies
- Single-tier edge → service.
- Tiered proxies: edge (public) → internal mesh or middle proxy → service.
- Sidecar/mesh: per-pod proxies enforce L7 policy, discovery, mTLS, and retries.

## Discovery
- Static lists (small systems), DNS-SRV, Consul, Kubernetes Endpoints, or xDS (Envoy). Discovery freshness directly impacts balancing accuracy and failover speed.

## Production checklist
- Decide: client-side vs server-side based on control and latency needs.
- Choose L4 vs L7 based on routing/policy requirements and performance budget.
- Define discovery source and TTLs; ensure health signals drive membership.
- Plan LB HA (N+1 instances, anycast VIPs, or managed cloud LBs).
