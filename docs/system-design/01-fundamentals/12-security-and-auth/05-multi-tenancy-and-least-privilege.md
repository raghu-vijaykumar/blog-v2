---
title: Multitenancy and Least Privilege
description: Designing tenant isolation and least‑privilege access across services and data stores. Covers scoping, data partitioning, key management, and operational guardrails with examples.
---

# Multitenancy and Least Privilege

## Overview
Multitenant systems must prevent noisy‑neighbor effects and cross‑tenant data access. Least privilege constrains identities, roles, and keys to the minimum required, reducing blast radius.

## What, Why, When (and when‑not)
What
- Isolation controls: identity scoping, data partitioning, rate limits/quotas, and network boundaries per tenant/tier.

Why
- Safety and fairness: protect data confidentiality and ensure predictable performance across tenants.

When
- Any shared platform serving multiple customers or business units.

When‑not
- Single‑tenant deployments can simplify, but retain least‑privilege by default.

## Core concepts and variants
- Identity scoping: embed `tenant_id` in subject and resource; deny cross‑tenant by default.
- Data isolation: per‑tenant schema/table, shard, or row‑level with proven filters and enforcement.
- Key isolation: envelope encryption with per‑tenant DEKs; optional per‑tenant KEKs in KMS.
- Quotas and fairness: per‑tenant rate and concurrency limits; priority tiers (gold/silver/bronze).

## Design decisions and trade‑offs
- Physical vs logical isolation: separate databases/projects per tenant improve isolation but increase ops cost; logical isolation scales better but requires rigorous enforcement and testing.
- Per‑tenant keys: strong cryptographic isolation but higher KMS usage and rotation overhead; group by tier to balance cost/security.

## Architecture and components
- Tenant registry and policy: maps tenant → allowed regions, features, quotas, encryption keys.
- Enforcement points: gateway (scope checks), services (PEP), data layer (RLS or query builders that inject tenant filters).

## Operational considerations
- Bootstrap/onboarding: automate tenant creation, keys, roles, quotas; idempotent and auditable.
- Testing: fuzz cross‑tenant access; simulate noisy neighbors; game days for quota enforcement.
- Offboarding: revoke access, rotate keys, delete/retain per policy; export tenant data packages.

## Examples
Example A (quantitative): Per‑tenant key cost
- 10k tenants, per‑tenant KEK with monthly rotation. KMS request cost ~1¢/10k ops; each rotation uses 2 requests (new + retire). Annual cost ~10k × 12 × 2 / 10k × $0.01 ≈ $0.024 plus envelope operations. Operational overhead, not raw cost, is the main concern.

Example B (architectural): Row‑level security (RLS)
- PostgreSQL RLS policy enforces `tenant_id = current_setting('app.tenant_id')::uuid`. App sets the setting from verified token claims in a transaction‑scoped session variable. All queries inherit policy without app code branching.

## Edge cases and anti‑patterns
- Trusting client‑supplied tenant IDs without verification; derive from token or mTLS identity only.
- Per‑endpoint exceptions for a tenant; codify in policy to prevent drift.

## Interactions with adjacent topics
- [Authorization Models](./04-authorization-models-rbac-abac-rebac.md) for tenant‑scoped roles.
- [Data Protection](./09-data-protection-pii-and-privacy.md) for classification and encryption per tenant.

## Production checklist
- Ensure every access path carries a verified `tenant_id` and enforces deny‑by‑default across boundaries.
- Implement quotas and fairness at edge and service; monitor per‑tenant saturation and errors.
- Use envelope encryption; decide per‑tenant vs per‑tier keys; document rotation.

## Interview framing checklist
- How would you implement tenant isolation in a shared Postgres cluster?
- What trade‑offs drive per‑tenant vs pooled encryption keys?

## References
- OWASP Multitenancy Cheat Sheet; NIST SP 800‑53 AC‑6 (Least Privilege)
