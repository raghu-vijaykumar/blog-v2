---
title: "Retries, Idempotency, and Client Behavior"
description: "Designing safe, efficient client behavior under throttling and failures: exponential backoff with jitter, retry budgets, idempotency keys, and standard headers."
---

## Overview
Client behavior determines whether throttling protects systems or amplifies incidents. Good clients retry sparingly with jitter, respect server signals, and use idempotency for safe replays.

## What, Why, When (and when-not)
What
- Strategies for retrying throttled/failed requests (429/5xx), using idempotency keys for non-idempotent methods, and honoring server guidance (Retry-After, RateLimit headers).

Why
- Prevent retry storms that worsen overload; ensure at-most-once semantics for writes; deliver better end-user experience under transient issues.

When
- Any SDK, mobile/web client, or service-to-service caller. Critical for POST/PUT/PATCH with side effects.

When-not
- One-off admin tools where manual retry control is acceptable; idempotency keys may be unnecessary for strictly idempotent operations.

## Core concepts and variants
- Backoff types: exponential, full-jitter, equal-jitter, and decorrelated jitter. Full-jitter/decorr-jitter typically minimize herd effects.
- Retry budgets: cap total retries as a percentage of original request volume (e.g., ≤ 10%). Prevents amplification during incidents.
- Per-call vs per-session budgets: enforce across all in-flight requests for a tenant/app to avoid aggregate overload.
- Idempotency keys: client-supplied unique keys for potentially duplicate writes so servers can deduplicate within a time window.
- Standard headers: Retry-After (seconds or HTTP date), and IETF RateLimit header fields (RateLimit-Limit/Remaining/Reset) for guidance.

## Design decisions and trade-offs
- Aggressiveness vs latency: faster retries improve user-perceived latency for transient faults but risk overload; budgets and jitter balance both.
- Client state complexity: idempotency stores require retention and eviction policies; too short a window risks duplicates on slow paths.
- Method semantics: GET/HEAD are idempotent; POST may be made idempotent via keys; PUT often idempotent if full state is provided.

## Algorithms and policies (conceptual)
Pseudocode: exponential backoff with full jitter (≤ 25 lines)
```pseudo
function retry_with_jitter(attempt, base=100ms, cap=5s):
  backoff = min(cap, base * 2^attempt)
  sleep(random(0, backoff))  # full jitter
```

Retry budget policy
- Maintain token bucket per client for retries: rate = α × original_rate (e.g., α=0.1). A retry consumes one token; deny when empty.

Idempotency key handling
- Server stores (idempotency_key → response) for a TTL (e.g., 24h). On duplicate POST with same key and payload hash, return original response.

## Architecture and components
- Client SDK: centralized retry logic, jitter, budgets, and idempotency key generation; pluggable per-endpoint policies.
- Server: idempotency store (fast KV or DB), payload hash to detect semantic dupes, TTL cleanup.

## Examples
Example A (quantitative): Retry amplification math
- If p = 5% of requests get 429 and client retries up to 3 times immediately, expected calls per original = 1 + p + p^2 + p^3 ≈ 1.0513 ⇒ +5.13% load even without jitter.
- With a 10% retry budget, only 10 retries per 100 original requests are allowed system-wide, capping amplification to 1.10×.

Example B (architectural): Idempotent POSTs in a payments API
- Client generates an Idempotency-Key GUID per charge attempt. Server stores key→result for 24h, keyed by merchant+key, with payload hash (amount, currency, source).
- Network flakiness causes duplicate POSTs; server replies with the original success without double-charging.

## Edge cases and anti-patterns
- Synchronized retries at boundaries (no jitter) cause traffic spikes; always use jitter.
- Reusing idempotency keys across distinct payloads must be rejected to avoid data corruption; compare payload hashes.
- Blindly retrying non-idempotent endpoints (e.g., POST without keys) can create duplicates.

## Interactions with adjacent topics
- [Backpressure & Shedding](./04-backpressure-signals-and-load-shedding.md): clients must honor 429 and Retry-After.
- [Models & Algorithms](./01-models-and-algorithms.md): RateLimit headers reflect algorithmic limits and reset times.

## Production checklist
- Implement full-jitter backoff; default cap ≤ 5s; expose per-endpoint overrides.
- Enforce retry budgets per client/tenant; log drops due to budget exhaustion.
- Support idempotency keys for write endpoints with 24h retention and payload hash checks.
- Emit RateLimit-* and Retry-After headers consistently.

## Interview framing checklist
- How do you prevent retry storms from mobile clients on flaky networks?
- How would you implement safe retries for a non-idempotent POST?

## References
- Exponential backoff and jitter (AWS Architecture Blog); IETF RateLimit header fields; HTTP Retry-After (RFC 9110)
