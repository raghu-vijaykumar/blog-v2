---
title: Data Protection - PII and Privacy
description: Handling sensitive data with classification, minimization, encryption/tokenization, access controls, retention, and privacy compliance. Includes examples and checklists.
---

# Data Protection: PII and Privacy

## Overview
Protect data throughout its lifecycle: collect minimally, classify, encrypt/tokenize, control access, and delete on schedule. Align with regulations without stalling delivery.

## What, Why, When (and when‑not)
What
- Data classification, collection minimization, encryption at rest/in transit, field‑level protection, access governance, and retention/deletion.

Why
- Reduces breach impact, meets regulatory duties, and builds user trust.

When
- Any processing of PII, PHI, PCI, or proprietary data; earlier is cheaper.

When‑not
- Avoid heavy frameworks when data is non‑sensitive; still apply sane defaults.

## Core concepts and variants
- Classification tiers: Public → Internal → Confidential → Restricted (PII/PHI/PCI).
- Field protection: tokenization/pseudonymization; format‑preserving encryption for legacy fields.
- Access: JIT elevation for support, dual‑control for decryption, fine‑grained data roles.
- Retention: TTL and deletion pipelines; legal hold exceptions.

## Design decisions and trade‑offs
- Tokenization vs encryption: tokens remove direct exposure but require lookups; encryption enables local processing but exposes ciphertext patterns unless randomized.
- Centralized data lake vs per‑service stores: governance vs blast radius; apply lake access controls + row/column security.

## Architecture and components
- Data catalog/classification service; KMS/tokenization service; DLP scanners; retention services; audit pipelines.

## Operational considerations
- Data subject requests: discover, export, delete; test end‑to‑end.
- Backups and analytics: ensure protected fields remain protected in ETL; re‑encrypt after incidents.

## Examples
Example A (quantitative): Retention storage savings
- If 10 TB/month of Restricted logs are retained for 30 days, reducing to 7 days saves ~23 TB over a quarter; verify compliance allows shorter retention.

Example B (architectural): Field‑level tokenization
- Store PAN/SSN as tokens; application holds only tokens; de‑tokenize in a segregated service with strict audit and JIT access. Analytics uses irreversible hashes for cohorting.

## Edge cases and anti‑patterns
- Sprinkling PII across logs and traces; default scrubbers and structured logging.
- Sharing raw datasets with partners; use privacy‑preserving aggregates.

## Interactions with adjacent topics
- [Secrets Management](./06-secrets-management-kms-hsm.md) for key handling; [Authorization](./04-authorization-models-rbac-abac-rebac.md) for data‑level access.

## Production checklist
- Classify data; minimize collection; encrypt/tokenize restricted fields; implement retention and DSR workflows.
- Scrub PII from telemetry by default; require explicit opt‑in for sensitive fields.

## Interview framing checklist
- How do you support “right to be forgotten” across microservices and backups?
- When is tokenization preferable to encryption?

## References
- NIST Privacy Framework, GDPR/CCPA summaries, PCI DSS, HIPAA
