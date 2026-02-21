---
title: Secrets Management, KMS, and HSM
description: Managing application secrets and cryptographic keys with KMS/HSM, envelope encryption, rotation, and distribution patterns. Includes diagrams and checklists.
---

# Secrets Management, KMS, and HSM

## Overview
Secrets (passwords, API keys, tokens) and cryptographic keys require secure storage, controlled access, rotation, and audit. Prefer managed KMS/HSM‑backed solutions and avoid secret‑in‑code or environment leaks.

## What, Why, When (and when‑not)
What
- Central secret store (Vault/Cloud Secrets) and KMS for encryption keys; envelope encryption for data at rest.

Why
- Reduces blast radius, provides rotation and audit, and simplifies compliance.

When
- Any production system handling credentials or sensitive data.

When‑not
- For local dev, use distinct, minimal secrets with short TTL; never reuse production credentials.

## Core concepts and variants
- Envelope encryption: DEK encrypts data; DEK encrypted by KEK (in KMS/HSM). Rotate KEK without re‑encrypting data.
- Secret distribution: pull (app fetches at startup/refresh) vs push (sidecar/agent injects). Prefer short‑lived credentials.
- Root of trust: HSM protects KEKs; attestations validate workloads before issuing secrets.

## Design decisions and trade‑offs
- Performance vs security: local caching of decrypted secrets reduces KMS latency but increases exposure; bound cache TTLs.
- Rotation frequency: too frequent increases outages; too rare increases risk. Automate and practice break‑glass.

## Architecture and components
- KMS/HSM, secret store, sidecars/agents, application libraries, and audit/SIEM.

Mermaid: envelope encryption flow
```mermaid
sequenceDiagram
  participant App as Application
  participant KMS as KMS/HSM
  participant Store as Secret Store
  App->>Store: Request secret (token)
  Store->>KMS: Decrypt secret (KEK)
  KMS-->>Store: Plaintext secret
  Store-->>App: Secret (short TTL)
  note over App: Cache minimally; refresh proactively
```

## Operational considerations
- Secret zero: avoid embedding root creds; bootstrap via platform identity (workload identity, IAM roles) and short‑lived tokens.
- Auditing: log reads/writes, rotation events, and failed access; alert on anomaly rates.
- Backup/DR: protect encrypted backups; rotate after incidents; test recovery.

## Examples
Example A (quantitative): KMS latency budgeting
- If KMS decrypt is 5–15 ms p50/95 and your P95 request budget is 100 ms, amortize by startup prefetch with 10–30 min refresh and lazy reload on rotation notices.

Example B (architectural): Sidecar injection
- Sidecar authenticates via workload identity, fetches secrets, renews leases, and exposes them via tmpfs volume to the app. Rotation is transparent; app watches file changes.

## Edge cases and anti‑patterns
- Committing secrets to git or container images; scanning must be part of CI and pre‑receive hooks.
- Long‑lived static DB passwords; prefer IAM‑auth or short‑lived credentials.

## Interactions with adjacent topics
- [Data Protection](./09-data-protection-pii-and-privacy.md) for field‑level encryption.
- [Transport Security](./07-transport-security-tls-mtls.md) for cert management.

## Production checklist
- Centralize secrets; use KMS‑backed encryption and rotate on a schedule and on demand.
- Eliminate secret zero via workload identity; implement lease/ttl and revocation.
- Monitor and alert on unusual secret access patterns.

## Interview framing checklist
- How would you design secret bootstrap without storing a root credential?
- How do you rotate database credentials across 100 services with near‑zero downtime?

## References
- NIST SP 800‑57 (Key Management), Cloud KMS/Vault docs, OWASP Secrets Management
