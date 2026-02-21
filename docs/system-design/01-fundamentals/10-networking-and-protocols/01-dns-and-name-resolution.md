---
title: DNS and Name Resolution
---

# DNS and Name Resolution

Overview
DNS maps stable names to changing network endpoints. In production, it is a global, cached, eventually consistent directory with tunable control over traffic steering and failover. Getting DNS right reduces cold‑start latency, speeds up failovers, and simplifies multi‑region routing.

What / Why / When
- What: A hierarchical, distributed key‑value system that resolves names (e.g., api.example.com) to records (A/AAAA, CNAME, TXT, SRV, HTTPS/SVCB).
- Why: Decouple clients from infrastructure, enable global routing, CDN offload, and controlled cutovers.
- When: Always for public entry points, internal service discovery (often via mesh/DNS), blue/green weight shifts, and disaster failover.

Core concepts and variants
- Zones and authorities: root → TLD → authoritative nameservers; delegation via NS records.
- Resolvers: stub (on host) → recursive (ISP/DoH/DoT) → authoritative.
- Record types: A/AAAA (IPv4/6), CNAME (alias), TXT, SRV (service discovery), MX (mail), SVCB/HTTPS (next‑gen service binding and ALPN hints).
- TTL and caching: Positive and negative caching; max‑age behaviors; resolver/personal DNS cache.
- Split‑horizon/private DNS: Different answers by source network.
- Geo/latency routing: Answer based on client location/probe data.
- Anycast DNS: Same IP announced from many PoPs; nearest responder wins (BGP).
- Health‑checked DNS: Managed providers remove unhealthy targets from answers.
- DNSSEC: Authenticity and integrity via signed zones (RRSIG, DS, DNSKEY).

Design decisions and trade‑offs
- TTL tuning: Short TTLs improve agility/failover but increase query volume and can be ignored by intermediaries; long TTLs reduce load but slow cutover.
- CNAME chains: Improve manageability but add lookups/latency. Prefer single‑hop CNAME to CDN/edge.
- DNS‑based load balancing: Simple and global, but coarse; per‑client caching and lack of instantaneous failover limit precision.
- Geo vs latency routing: Geo is predictable; latency probes adapt faster but can misroute during incidents.
- DNSSEC: Stronger integrity; operational complexity and larger responses (EDNS0) can cause truncation/fragmentation issues.

Algorithms/policies (conceptual)
Pseudo‑policy for weighted routing with health:
```
function dns_answer(name, pool):
  healthy = filter(pool, target => target.isHealthy)
  if empty(healthy): return SERVFAIL or fallback_cname
  return weighted_random_sample(healthy, k=answer_size)
```
Failover policy: If primary region health is less than threshold, reduce its weight exponentially every N seconds until 0; increase secondary accordingly (DNS slow‑start to avoid thundering herd).

Architecture and components
```mermaid
flowchart LR
  A[Client App] --> B[Stub Resolver]
  B --> C[Recursive Resolver\n(DoH/ISP/Corp)]
  C --> D[Root]
  D --> E[TLD]
  E --> F[Authoritative NS\n(Managed DNS)]
  F -->|A/AAAA/CNAME| G[Answers]
  G --> C --> B --> A
```

Operational considerations
- Capacity: QPS at authoritative; plan for peak TTL expirations and negative caching. Enable anycast for global reach.
- Failure modes: NXDOMAIN caching, stale resolvers, EDNS0 size and UDP fragmentation causing timeouts; fallback to TCP.
- Observability: Query logs, per‑name QPS, SERVFAIL/NXDOMAIN rates, geo distribution, answer set entropy.
- Runbooks: TTL roll‑down before traffic shifts; validate resolvability from multiple networks (dig +1 @resolver); pre‑warm recursive caches.
- Security: DNSSEC, registrar lock, 2FA on providers; monitor NS/DS drift.

Examples
1) Quantitative — TTL and failover lag
- If 60% of clients honor TTL and 40% use a 5‑minute floor, with configured TTL=30s, expected median cutover is ~30s, but p90 approaches 5 minutes. To keep `p90<2m`, either reduce floor via enterprise resolvers or use app‑level retries to new endpoints.

2) Architectural — Multi‑region weighted routing
- Use a CNAME api.example.com → api.global.example.net managed by provider with health checks. Start with weights: region‑A=100, region‑B=0. During canary, shift 10% every 10 minutes by adjusting weights; if region‑B error rate>1%, roll back by halving B’s weight every interval. Keep TTL=30–60s and ensure app connection pools honor DNS re‑resolves.

Edge cases and anti‑patterns
- Sticky corporate resolvers ignoring low TTLs; captive portals hijacking DNS; long CNAME chains causing lookup latency; wildcard records shadowing specific names; forgetting negative TTL when fixing NXDOMAIN.

Interactions with adjacent topics
- Load balancing: DNS steering vs L7 LB; combine with health‑checked LBs.
- Availability: Coordinates with timeouts/retries for faster failover.
- Security: DNSSEC, mTLS at later layers; SVCB/HTTPS records advertise ALPN.
- Observability: Correlate spikes in SERVFAIL with provider issues.

Production checklist
- [ ] Registrar security (lock, 2FA), NS/DS records audited
- [ ] Authoritative under anycast with multi‑provider or multi‑region
- [ ] TTLs set per record purpose (failover vs stability)
- [ ] Health‑checked answers for active pools
- [ ] Synthetic checks from various networks; negative caching verified
- [ ] Document rollback (weights/TTLs) and rehearse

Interview framing checklist
- How would you design DNS failover for a multi‑region API without client changes?
- What TTL would you choose and why? How do resolvers impact it?
- When to use DNS steering vs L7 load balancer weighting?

References
- RFC 1034/1035 (DNS), RFC 7871 (EDNS Client Subnet), RFC 4033–4035 (DNSSEC)
- Provider docs: Route 53, Cloudflare, NS1 latency routing/health checks
- IETF SVCB/HTTPS (RFC 9460)
