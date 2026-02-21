---
title: Security, Privacy, Multitenancy
---

# Security, privacy, and multitenancy

Caching intersects with security and privacy in two main ways: unintentionally sharing sensitive data and letting tenants impact each other’s performance. This page outlines hardening strategies for each cache layer.

See also:
- Multi‑level caching: ./06-multilevel-hierarchical.md
- Consistency & invalidation: ./05-consistency-invalidation.md

## Key risks to guard against

- Data leakage at the edge
  - Public/shared caches may serve one user’s personalized response to another if keys or Vary headers are wrong.
- Cache poisoning
  - Attackers inject untrusted content (e.g., via query params) that becomes cached and then served broadly.
- Timing/XS‑Leaks
  - Cross‑user timing differences can reveal whether content was cached or not; avoid using cache state to infer private data.
- Multi‑tenant interference
  - One tenant can hog capacity, causing evictions, high latency, or errors for others (noisy neighbor).

## Defensive design patterns

Edge/CDN
- Do not cache personalized responses publicly
  - Use Cache‑Control: private or no‑store for per‑user content; prefer caching page shells and composing private fragments at origin.
- Constrain cache keys
  - Avoid Vary: Authorization. Instead, use surrogate keys, cookies, or headers that your CDN is allowed to vary on safely (e.g., locale, device).
- Purge safely
  - Prefer tag/segment purge; restrict purge APIs with auth and audit. Log all purges.

Reverse proxy/service boundary
- Scope cache keys to principals
  - Include tenant and user/role context: `tenant:<t>:role:<r>:resource:v1:<id>`.
- Validate inputs rigorously
  - Normalize and whitelist query params/headers that influence cache keys to prevent key explosion and poisoning.
- Short TTL for sensitive data
  - For auth/session/PII‑derived responses, use tiny TTLs or avoid caching entirely.

Service L2/L1 caches
- Encrypt in transit (and at rest if supported)
  - Use TLS to the cache; enable AUTH/ACLs in Redis and network‑level isolation.
- Do not store secrets or long‑lived tokens
  - If necessary, store opaque references with short TTL; rotate frequently.
- Idempotent invalidation
  - On permission or role changes, invalidate related keys immediately to prevent stale authorization decisions.

## Multitenancy isolation

- Namespacing
  - Prefix keys with tenant ID: `tenant:<t>:<domain>:vX:<id>`. Makes bulk invalidation and quotas tractable.
- Quotas and rate limits
  - Per‑tenant QPS caps and memory quotas (by prefix) to prevent noisy neighbors.
- Sharding and placement
  - For very large tenants, partition their keys across dedicated shards/pools to isolate impact and scale independently.
- Observability per tenant
  - Track hit/miss, memory usage, and eviction rate by tenant prefix; alert when tenants approach quotas.

## Example key scoping
```text
public:user:v1:123
tenant:acme:user:v1:123
tenant:acme:role:admin:feature-flags:v3
```

## Operational checklist

- Edge
  - Cache‑Control for each route reviewed; no Vary: Authorization; purge endpoints locked down and audited.
- Service
  - Key construction includes tenant/user context where needed; TTLs minimal for sensitive data; invalidation on auth changes.
- Cache infra
  - TLS + AUTH/ACLs enabled; per‑tenant quotas; monitoring for key‑space growth by prefix; backups disabled for secret‑like ephemeral data.
