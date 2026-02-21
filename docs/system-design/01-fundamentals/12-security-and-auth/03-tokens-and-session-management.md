---
title: Tokens and Session Management
description: Token formats (JWT vs opaque), validation, rotation and revocation strategies, and secure session management with cookies. Includes examples and pseudocode.
---

# Tokens and Session Management

## Overview
Access tokens carry authorization context to APIs; sessions bind a browser/device to a server‑side identity. Choose formats and lifetimes that minimize replay risk while preserving performance.

## What, Why, When (and when‑not)
What
- JWT (self‑contained) or opaque (reference) access tokens; refresh tokens to mint new access tokens; secure, short‑lived browser sessions.

Why
- Balance stateless verification performance with revocation needs and least privilege. Reduce token theft blast radius.

When
- Internet‑facing APIs, mobile/SPA apps, and meshes that need identity propagation.

When‑not
- In tightly coupled internal RPC with mTLS identities, consider header‑level identity only and local authz.

## Core concepts and variants
- JWT: signed (JWS) tokens with claims (`iss`, `sub`, `aud`, `exp`, `iat`, `jti`, scopes). Validate signature and claims locally.
- Opaque: random handle; API calls introspect/lookup in an authorization server/cache.
- Refresh token rotation: one‑time use; detect reuse to revoke session.
- Sender constraining: DPoP or mTLS‑bound tokens to a key/certificate to prevent replay.
- Cookies: HttpOnly, Secure, SameSite (Lax/Strict); set `__Host-` prefix where possible; short lifetimes.

## Design decisions and trade‑offs
- JWT pros: low latency, offline validation, good for high‑QPS; cons: revocation hard, risk if leaked before expiry.
- Opaque pros: revocable, centralized control; cons: adds latency/dependency; must scale introspection cache.
- Hybrid: JWT for standard scopes with short TTL; opaque for high‑risk scopes or admin actions.

## Algorithms and policies (conceptual)
Pseudocode: opaque token introspection cache (≤ 30 lines)
```pseudo
function validate_access_token(token):
  entry = local_cache.get(token)
  if entry and entry.exp > now():
    return entry.claims
  # Cache miss or expired; call AS
  resp = POST introspection_endpoint { token }
  if not resp.active:
    raise Unauthorized
  ttl = min(resp.exp - now(), MAX_CACHE_TTL)
  local_cache.set(token, {claims: resp.claims, exp: resp.exp}, ttl)
  return resp.claims
```

## Architecture and components
- Token issuers (IdP/AS), JWKS publishers, API gateways/service validators, revocation lists, and caches.
- Session stores: server‑side (DB/Redis) vs stateless signed cookies (only for low‑risk, minimal state).

## Operational considerations
- Key rotation: rotate signing keys; alert on unknown `kid`.
- Blacklist/deny‑list: use only for narrow, short‑lived revocations; prefer short TTL + rotation.
- Logout: revoke refresh tokens; clear cookies; consider device list for user self‑revocation.

## Examples
Example A (quantitative): JWT TTL selection
- Choose 5–10 min TTL for access tokens. With 100k rps API and 99% cache hit for JWKS/validators, signature checks add ~50–100µs median; ensure p99 budget accommodates validation.

Example B (architectural): Browser session hardening
- Store only a session identifier in a cookie with `HttpOnly; Secure; SameSite=Lax; Path=/;` and `__Host-` prefix. Map to server‑side session containing minimal attributes and a CSRF token for state‑changing HTML forms.

## Edge cases and anti‑patterns
- Putting PII or authorization decisions in unsigned cookies or localStorage; prefer server‑side state or signed, minimal JWTs.
- Long‑lived refresh tokens on mobile without rotation; rotate and bind to device key when feasible.

## Interactions with adjacent topics
- [Identity](./02-identity-oauth2-oidc.md) for flows; [Authorization Models](./04-authorization-models-rbac-abac-rebac.md) for scopes/claims semantics.

## Production checklist
- Decide on JWT vs opaque by risk and performance; define TTLs and rotation.
- Enforce cookie security attributes; avoid storing sensitive data client‑side.
- Implement token introspection cache or JWKS caching with retries/backoff.

## Interview framing checklist
- How do you achieve immediate revocation with JWTs? What trade‑offs?
- What cookie attributes prevent common web attacks and why?

## References
- RFC 7519 (JWT), RFC 7662 (Introspection), OWASP Session Management
