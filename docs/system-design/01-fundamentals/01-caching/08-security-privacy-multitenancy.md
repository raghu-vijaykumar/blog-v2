---
title: Security, Privacy, Multitenancy
---

# Security, privacy, and multitenancy

Key risks
- Cache poisoning, XS-Leaks via shared caches
- Personalized data leakage at edge/CDN

Safer designs
- Avoid Vary: Authorization; scope keys instead (e.g., user or role in key)
- Separate public vs private cache layers; use private caches for personalized data

Tenant isolation
- Per-tenant namespace and quotas; rate limit to avoid noisy neighbors

Example key scoping
```text
public:user:v1:123
tenant:acme:user:v1:123
tenant:acme:role:admin:feature-flags:v3
```
