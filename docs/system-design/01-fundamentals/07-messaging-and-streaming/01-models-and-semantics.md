---
title: Models & Semantics (Queues vs Logs; Ordering; Idempotency)
description: Defines queues vs logs, delivery semantics (at-most/at-least/exactly-once), ordering scope, idempotency, and practical patterns to achieve correctness under retries and replay.
---

## Overview
Messaging models fall broadly into task queues and append-only logs. Queues focus on work distribution with per-message acknowledgement; logs focus on durable, ordered streams replayable by many consumers. Correctness hinges on delivery semantics, ordering scope, and idempotency.

## What, Why, When (and when-not)
What
- Models: queue (transient work item with ack) vs log/stream (durable ordered sequence with offsets).
- Semantics: at-most-once, at-least-once, exactly-once processing (achieved through idempotency, transactions, or both).

Why
- Decouple producers/consumers in time and rate; enable fan-out and replay. Pick semantics to bound data loss/duplication risks relative to cost and latency.

When
- Queue: background jobs, one-and-done tasks, workflow orchestration with limited ordering guarantees.
- Log: CDC/event sourcing, analytics pipelines, multi-sink fan-out, systems needing replay and time travel.

When-not
- Avoid global total ordering if partitioned ordering suffices—global order throttles throughput. Avoid at-most-once if retries are required for reliability.

## Core concepts and variants
- Message vs event: tasks often carry intent; events describe facts that happened. Events are append-only and safe to replay; tasks may be unsafe to replay.
- Topic/Queue: named stream; topics can be partitioned; queues may be FIFO or best-effort ordered.
- Partition and offset: ordering is guaranteed per partition; offsets or sequence numbers advance as consumers process.
- Acknowledgement: ack/nack (queue) or offset commit (log). Redelivery happens on nack/timeout (queue) or on reset offset (log).
- Idempotency: ensure repeated processing yields the same end state. Techniques: idempotency keys, dedup tables, upserts, commutative operations, CRDTs.
- Exactly-once processing: practically “at-least-once delivery + idempotent processing” or end-to-end transactions (e.g., Kafka EOS with transactional producers/consumers and transactional sink writes).

## Design decisions and trade-offs
- Ordering vs parallelism: tighter ordering (FIFO/global) limits concurrency. Partitioned ordering enables scale but only per-key order.
- Durability vs latency: synchronous replication and fsync increase safety but add tail latency.
- Throughput vs cost: long retention and compaction increase storage IO and cost; queues with short visibility timeouts reduce storage but lose replay.
- Producer vs consumer complexity: pushing idempotency to producers (dedupe keys) vs consumers (upsert-by-key, de-dupe window) affects coupling and reprocessing ease.

## Algorithms/policies (conceptual)
Idempotent consumer (upsert-by-key)
```pseudo
onMessage(msg):
  // msg.key is stable idempotency key (e.g., order_id:event_seq)
  begin txn
    if dedup_table.contains(msg.key):
      commit txn; return // already processed
    applyBusinessUpsert(msg)
    dedup_table.insert(msg.key, processed_at=now())
  commit txn
```

Offset-commit policy (logs)
```pseudo
// Process-batch-then-commit to avoid losing progress with failures
batch = poll()
processAll(batch)
commitOffsets(batch.maxOffset)
```

## Architecture and components
- Producers, brokers/servers, storage (segments), coordinators (group membership/rebalance), offset/ack stores, schema registry, DLQ/retry topics.

```mermaid
flowchart LR
  P1[Producer(s)] -->|publish| T1[(Topic / Queue)]
  subgraph Broker
    T1 --> S1[Partition 0]
    T1 --> S2[Partition 1]
  end
  S1 --> CG[Consumer Group]
  S2 --> CG
  CG --> C1[Consumer A]
  CG --> C2[Consumer B]
```

## Operational considerations
- Retention: time/size-based; plan disk headroom for worst-case lag.
- Compaction: for key-based latest-value semantics; understand tombstones and cleanup delays.
- Schema evolution: use registries and backward/forward compatible encoding (Avro/Protobuf/JSON-Schema).
- Visibility/timeout (queues): set timeouts > p99 processing time with jitter to reduce duplicate redeliveries.

## Examples
Example A (quantitative): partition and throughput sizing
- Target: 100k msgs/s, avg 1 KB, per-consumer core handles ~5k msgs/s.
- Required parallelism ≈ 100k / 5k = 20 partitions and ≥ 20 consumer threads. Add 25% headroom → 25 partitions.

Example B (architectural): outbox pattern for CDC
- Service writes business row and an “outbox” row in same DB transaction.
- A CDC process tails the outbox table to a log (Kafka topic) with an idempotency key derived from primary key + version.
- Downstream consumers upsert into materialized views. Replays are safe due to idempotent keys.

## Edge cases and anti-patterns
- Using timestamps as unique ids without monotonicity guarantees → dedupe collisions under clock skew.
- Large messages (>1–5 MB) in brokers tuned for small messages → head-of-line blocking and memory pressure; store payloads in object storage and pass pointers.
- Relying on consumer-side “exactly-once” without idempotent sinks → duplicates leak into state.

## Interactions with adjacent topics
- See [Consistency & CAP](../05-consistency-and-cap/) for ordering and session guarantees.
- See [Databases & Storage](../06-databases-and-storage/) for CDC, compaction, and materialized views.
- See [Rate Limiting & Backpressure](../08-rate-limiting-and-backpressure/) for end-to-end flow control.

## Production checklist
- Define semantics per stream/queue (delivery, ordering scope).
- Choose partition key and expected consumer parallelism; compute partitions with headroom.
- Enforce idempotency (keys/dedup tables/upserts) and document replay procedure.
- Establish retention and compaction policies; size disks and IO accordingly.
- Version schemas with compatibility rules and CI checks.

## Interview framing checklist
- How to achieve “exactly-once” in practice across a producer, broker, and sink?
- When is FIFO required vs partitioned ordering sufficient?
- How would you design idempotency for payment or order processing?

## References
- Kleppmann, Designing Data-Intensive Applications (Ch. 11)
- Kafka: Exactly-Once Semantics; RabbitMQ: Acknowledgement and Redelivery; AWS SQS/SNS/Kinesis docs
