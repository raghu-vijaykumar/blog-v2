---
title: Session Affinity and State
---

# Session affinity and state

Prefer stateless services; use affinity only when necessary.

## Techniques
- Cookie-based stickiness: LB sets a cookie mapping client → backend.
- Header/IP hash: coarse and may break with NAT/proxies.
- Consistent hash on user/session ID: preserves distribution, avoids LB-side state.

## Trade-offs
- Reduces effective balancing; impacts failover.
- Can cause uneven load if certain users are heavy.
- Requires explicit fallback when a bound backend is unhealthy.

## Safer patterns
- Store session data in a shared store (Redis/DB) → keep app stateless.
- Bind affinity to a token (JWT claim) but allow remap on failure with graceful degradation.
- Limit stickiness TTL; rotate keys during deploys.

## Production checklist
- Justify why affinity is needed; define TTL and fallback.
- Ensure data needed for a request lives off the instance (shared store) where possible.
- Monitor per-backend stickiness distribution and hot spots.
