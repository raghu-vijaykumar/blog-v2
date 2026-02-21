---
title: Multi-level and Hierarchical Caching
---

# Multi-level and hierarchical caching

Multi-level caching layers different caches to get the best mix of latency, offload, and correctness. This page explains when and how to stack layers (browser, CDN/edge, reverse proxy, service L1/L2, DB), how to coordinate freshness and invalidation across them, and what to measure.

See also:
- Foundations and mental models: ./01-foundations.md
- Distributed caches (Redis/Memcached): ./04-distributed-caches.md
- Consistency, coherence, invalidation: ./05-consistency-invalidation.md

## Typical layering and responsibilities

- Browser cache
  - Zero-RTT for static assets; respects HTTP Cache-Control/ETag.
  - Responsibility: speed for repeat navigations; keep private to the user.
- CDN/Edge cache
  - Global offload for public, cacheable content and API responses safe to share.
  - Responsibility: byte hit ratio, shield origins, fast TTL‑based freshness, tag/purge support.
- Reverse proxy (e.g., Nginx/Varnish/Envoy)
  - Origin‑facing HTTP cache with richer policy (SWR, coalescing) close to your services.
  - Responsibility: request coalescing and revalidation, enforcing cacheability rules, auth‑aware bypass.
- Service caches
  - L1: in‑process per instance. Ultra‑low latency but not shared.
  - L2: distributed cache (Redis/Memcached). Shared across instances, controllable TTL/eviction.
  - Responsibility: absorb read load for dynamic objects and query results with bounded staleness.
- Database/page cache
  - DB‑internal caching of pages/rows. Complements app caches; last line before disk.

Design goal: push immutable and public content outward (browser/CDN), keep personalized or sensitive data within the service boundary (L1/L2), and ensure a clear invalidation owner for each layer.

## Coordinating freshness across layers

- Stagger TTLs
  - Shorter at the edge, longer closer to origin (Edge 30–300s, Proxy 1–10m, L2 5–30m, L1 30–120s) depending on volatility.
  - Add TTL jitter at each layer to avoid synchronized expiries and thundering herds.
- Revalidation where supported
  - Use ETag/If‑None‑Match or Last‑Modified/If‑Modified‑Since between proxy→service and service→origin when feasible.
  - Prefer 304 revalidation at the proxy/edge over full misses.
- Soft TTL and serve‑stale
  - Configure proxies and service caches to serve stale briefly while a single request refreshes (see ./05-consistency-invalidation.md for SWR/coalescing patterns).
- Bypass heuristics
  - For low‑hit or highly personalized routes, bypass edge caches (Cache‑Control: private, no‑store) and rely on L1/L2 with scoped keys.

## Keying and HTTP cache semantics

- Stable, versioned URLs for immutable assets
  - Use content hashes in filenames (app.abc123.js). Set long TTLs (months) and immutable directives at CDN and browser.
- Vary and key scoping
  - Be deliberate with Vary headers (e.g., Accept‑Encoding, Accept‑Language). Avoid Vary: Authorization; prefer private caches for auth‑sensitive content.
  - For service caches, include context in keys: `tenant:<t>:user:v1:<userId>`; see ./08-security-privacy-multitenancy.md.
- Surrogate keys (edge tag purge)
  - When CDN supports it, tag responses (e.g., Surrogate‑Key: product:123 category:shoes). Purge by tag on updates to invalidate many objects safely.

## Invalidation across layers

- Edge/CDN
  - Purge by URL for specific objects; prefer tag/segment purge when supported to avoid scanning keys.
  - For immutable assets, deploy new versioned URLs instead of purging (cache‑busting).
- Reverse proxy
  - Respect upstream 5xx/timeout policies; serve stale on error to protect origins.
- Service L2 and L1
  - On write: origin update → L2 invalidate/update → broadcast to clear L1 on all instances (pub/sub). See ./05-consistency-invalidation.md for examples.

Example: Envoy/Proxy “serve stale on error” concept
```text
# Pseudocode policy
if cache_entry.is_fresh():
  return cache_entry
elif cache_entry.is_stale() and origin.is_unhealthy():
  return cache_entry with header X-Cache: STALE
else:
  coalesce_and_refresh()
```

## Personalization strategies at scale

- Keep private at the edge
  - Use Cache‑Control: private or no‑store for per‑user responses; avoid sharing personalized content at CDN.
- Hole‑punching/ESI (when needed)
  - Cache the page shell at CDN and compose personalized fragments at the origin/service. Prefer server‑side composition over client‑side when SEO/TTFB matters.
- Session‑scoped L1
  - For session‑heavy apps, keep a tiny in‑process cache per instance to avoid repeated DB calls within a session.

## Request coalescing: across layers

- Edge: ensure only one fetch per URL during a miss (CDN shielding, collapsed forwarding if available).
- Proxy: singleflight for upstream misses.
- Service: singleflight around expensive L2/DB calls per key.

## Pre‑warming and warmup flows

- Pre‑publish warmers
  - Before a big launch or deploy, prime hot URLs at CDN/proxy and prime hot entities in L2 to avoid cold‑start latency.
- Read‑through warmup
  - After invalidation, allow first request to refresh while others receive stale.

## Rules of thumb

- Immutable assets: months‑long TTL + versioned filenames; never purge, always roll forward.
- Dynamic public APIs: short TTL + revalidation; use edge tag purge on updates.
- Personalized responses: private/no‑store at edge; cache safely at L1/L2 with scoped keys.
- L1 TTL shorter than L2; L2 TTL shorter than proxy; proxy shorter than CDN.

## Production checklist (multi‑level)

- Cache‑control policy documented per route (public vs private, TTLs, revalidation).
- Coalescing enabled at proxy/service; stale‑while‑revalidate configured.
- Edge purge strategy defined (URL vs tag). Versioned asset pipeline in place.
- Invalidation fan‑out from origin → L2 → L1 broadcast implemented and idempotent.
- Observability: per‑layer hit ratios (request and byte), revalidation rate, stale‑served, origin offload.

