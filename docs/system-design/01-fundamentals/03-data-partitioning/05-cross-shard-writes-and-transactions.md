---
title: Cross‑shard Writes & Transactions
description: Patterns for safe writes across shards—idempotency, 2PC vs Sagas, unique constraints, and outbox/CDC.
---

## Overview
Aim to keep writes single‑shard; when cross‑shard coordination is required, choose between atomic 2PC and compensating Sagas based on latency and business semantics.

## Single‑shard vs cross‑shard
- Single‑shard
  - Full ACID with local transaction; enforce uniqueness in a shard namespace (e.g., `(tenant_id, order_id)`).
- Cross‑shard
  - Minimize scope (2–3 shards max); design for partial‑failure semantics.

## Idempotency and exactly‑once illusions
- Use idempotency keys or natural keys; dedupe on the write path.
- Store request IDs with TTL for at‑least‑once retry safety.

## 2PC (Two‑Phase Commit)
- Coordinator asks shards to PREPARE, then COMMIT or ABORT.
- Pros: atomicity; Cons: locks resources, sensitive to coordinator failure.

Pseudocode: simplified 2PC
```pseudo
txid = new_tx()
ok = true
for shard in shards:
  ok &= shard.prepare(txid, mutation)
if ok: for shard in shards: shard.commit(txid)
else: for shard in shards: shard.abort(txid)
```

## Sagas
- Sequence of local transactions with compensating actions.
- Pros: resilient to long‑running workflows; Cons: eventual consistency visible to users.

Example: create order (shard A) → reserve inventory (shard B) → charge payment (external) → confirm order. Compensations reverse steps on failure.

## Unique constraints and foreign keys
- Avoid hard global FKs; enforce via application checks and namespaces.
- For global uniqueness, centralize a small keyspace with bounded QPS and caching, or pre‑allocate ranges per shard.

## Outbox and CDC
- Use outbox table to record domain events within the local transaction; a CDC process publishes to a bus and coordinates downstream updates.

## Production checklist
- Prefer single‑shard writes; co‑locate entities by routing key.
- Choose 2PC only when atomicity is mandatory and latency budgets allow; otherwise prefer Sagas.
- Implement idempotency tokens, dedupe, and bounded retries.
- Document user‑visible consistency and reconciliation flows.
