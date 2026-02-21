---
title: Messaging & Streaming
---

# Messaging & Streaming

## Overview
Messaging decouples producers and consumers through asynchronous delivery, while streaming treats data as an ordered, durable log of events. Together they enable elasticity, resilience, and near–real-time data movement across services, analytics, and storage systems.

## What, Why, When (and when‑not)
What
- Core models: queues vs logs; pub/sub topics and partitions; consumer groups and offset tracking.
- Delivery semantics: at‑most‑once, at‑least‑once, and exactly‑once (effective) processing via idempotency and transactions.

Why
- Reduce coupling (time, load, and failure isolation) between producers and consumers; enable fan‑out to multiple downstream systems; capture durable history for reprocessing.

When
- Use queues to distribute discrete work items with per‑message acknowledgement and limited ordering needs.
- Use logs/streams to maintain ordered, replayable histories for analytics, CDC, and multi‑sink fan‑out.

When‑not
- Avoid introducing a broker just to serially call a single worker—synchronous RPC might be simpler. Avoid global total ordering unless it is strictly required; it is costly and limits throughput.

## Module contents
- [01 — Models & Semantics](./01-models-and-semantics.md): Queues vs logs; delivery semantics; ordering; idempotency.
- [02 — Topics, Partitions & Ordering](./02-topics-partitions-and-ordering.md): Topic design, partitioning keys, rebalancing, hotspots.
- [03 — Pub/Sub, Consumer Groups & Scaling](./03-pubsub-consumer-groups-and-scaling.md): Group coordination, throughput math, elasticity.
- [04 — Delivery, Retries, DLQ & Idempotency](./04-delivery-semantics-retries-and-dlq.md): Backoff, poison pills, dedupe, replay.
- [05 — Logs vs Queues & Storage Characteristics](./05-logs-vs-queues-and-storage-characteristics.md): Retention, compaction, offsets, durability.
- [06 — Stream Processing & State](./06-stream-processing-and-state.md): Windows, watermarks, checkpoints, "exactly‑once" designs.
- [07 — Operations, Observability & Runbooks](./07-operations-observability-and-runbooks.md): Lag SLOs, scaling, schema evolution, failure drills.
- [08 — Selection Guide & Comparisons](./08-selection-guide-and-comparisons.md): Kafka/Pulsar vs RabbitMQ/NATS vs SQS/SNS/Kinesis vs Pub/Sub.
- [09 — Case Studies](./09-case-studies.md): E‑commerce orders, CDC pipelines, IoT telemetry, search indexing.

## Interactions with adjacent topics
- See [Consistency & CAP](../05-consistency-and-cap/) for ordering, quorums, and client techniques.
- See [Rate Limiting & Backpressure](../08-rate-limiting-and-backpressure/) for end‑to‑end flow control.
- See [Databases & Storage](../06-databases-and-storage/) for CDC, materialized views, and durability paths.

## Production checklist (quick)
- Define delivery semantics and ordering scope per stream/queue.
- Choose partition keys and expected consumer group size; compute required partitions for peak throughput.
- Specify retry policy, DLQ criteria, and idempotency strategy; document replay procedure.
- Establish lag SLOs, offset/lag monitoring, and alerting; plan capacity and retention budgets.
- Prove disaster procedures: broker failover, consumer rebalancing, DLQ drains, and replay drills.

Proceed to the sub‑pages for detailed guidance and worked examples.
