---
title: Operations, Observability & Runbooks
description: Capacity planning, scaling and upgrades, monitoring and alerting, disaster readiness, and concrete runbooks for brokers, consumers, and schema evolution.
---

## Overview
Operating messaging/streaming systems requires disciplined capacity planning, explicit SLOs (produce/consume latency, lag), robust monitoring, and rehearsed failure procedures. This page provides pragmatic guidance and runbooks.

## What, Why, When (and when-not)
What
- Operational levers: partitions, replication, retention, consumer group sizing, schema management, and deployment strategy.

Why
- Avoid silent data loss or unbounded lag; keep latency predictable under upgrades and failures; enable safe evolution of schemas and topology.

When
- Revisit capacity quarterly and before major traffic events. Rehearse disaster runbooks at least twice a year.

When-not
- Avoid ad-hoc partition count changes during incidents; prefer pre-planned headroom and autoscaling of consumers first.

## Core concepts and variants
- SLOs: produce p99 latency, fetch p99 latency, max acceptable consumer lag (seconds/messages), availability of control plane (controller/metadata service).
- Capacity headroom: 30–50% headroom on broker disk/IO; 25–40% on partitions vs active consumers.
- Schema governance: registry with compatibility rules (backward/forward/full); CI checks gate deploys.
- Upgrades: rolling restarts with rack-awareness; leader election throttling; cooperative rebalances for consumers.

## Design decisions and trade-offs
- Replication factor vs latency/cost: RF=3 is common; higher RF improves durability but increases produce latency and storage cost.
- Retention windows: long windows enable reprocessing but cost storage/IO; pair with tiered storage where supported.
- Autoscaling consumers vs adding partitions: scaling groups is immediate; raising partitions is impactful and durable but operationally heavy.

## Architecture and components
- Brokers/servers, controllers/metadata nodes, ZooKeeper-less controllers (newer Kafka), consumer fleet with autoscaling, schema registry, observability stack (Prometheus/Grafana/CloudWatch/Stackdriver).

```mermaid
flowchart LR
  subgraph Control
    CTRL[Controller / Metadata]
    SR[Schema Registry]
  end
  subgraph Data Plane
    B1[Broker 1] B2[Broker 2] B3[Broker 3]
  end
  P[Producers] --> B1 & B2 & B3
  B1 & B2 & B3 --> C[Consumer Groups]
  CTRL --> B1 & B2 & B3
  SR --> P
  SR --> C
```

## Operational considerations
- Disk and IO: keep ≥ 30–40% free; baseline compaction/segment churn; separate logs from OS disks.
- Network: validate NIC capacity vs aggregate produce/replicate egress; MTU/mss tuning; avoid packet drops.
- Security: TLS, mTLS for clients, ACLs per topic/group; rotate credentials safely.
- Change management: stagger deploys; canary topics/consumers; freeze windows during peak events.
- Schema: enforce compatibility; add fields in backward-compatible ways; migrate consumers gradually.

## Examples
Example A (quantitative): lag budget and scaling
- Business SLO: end-to-end under 60s; producer→broker p99=50ms, broker→consumer fetch p99=100ms, processing p99=200ms.
- Remaining budget for lag = 60s − (0.35s) ≈ 59.65s.
- With incoming 30k msgs/s and per-thread 2k msgs/s, threads needed to keep lag < 60s when backlog=0: ceil(30k/2k)=15. For backlog of 900k msgs (30s at peak), extra threads to drain within 60s: ceil(900k/60/2k)=8. Total ≈ 23 threads.

Example B (architectural): blue–green broker upgrade
- Add new broker nodes (green) to cluster; throttle leader reassignments.
- Migrate partitions gradually; monitor under-replicated partitions and ISR.
- Drain old brokers (blue) and decommission after retention grace.

## Edge cases and anti-patterns
- Repartitioning during incident response → destabilizes consumers and increases duplicates.
- Disabling authentication for “temporary fix” → long-lived security debt and outages later.
- Ignoring DLQ growth → silent user-impacting data loss when DLQ retention expires.

## Runbooks (actionable)
- Broker under-replicated partitions
  - Identify affected partitions; check broker health/disk/network.
  - Increase replica fetcher threads/throughput cautiously; verify ISR growth.
  - If disk full, expand volume or reduce retention immediately; avoid deleting active segments.
- Consumer lag spike
  - Verify downstream sink latency; enable backpressure; increase consumer threads/instances temporarily.
  - Pause non-critical topics; drain backlog; resume gradually.
- Schema incompatibility
  - Stop producers; roll back to last compatible schema; replay DLQ/correction topic after fix.

## Interactions with adjacent topics
- See [Availability & Fault Tolerance](../09-availability-and-fault-tolerance/) for failure domains.
- See [Rate Limiting & Backpressure](../08-rate-limiting-and-backpressure/) for intake control.
- See [Observability](../11-observability/) for standard metrics and SLOs.

## Production checklist
- Define and monitor SLOs (produce/fetch p99, lag, throughput, ISR size, UPR).
- Maintain broker disk headroom ≥ 30–40%; validate NIC and replication bandwidth.
- Enforce schema compatibility in CI; document rollout/rollback steps.
- Rehearse DLQ replay and disaster failover twice a year.

## Interview framing checklist
- How do you budget and enforce consumer lag SLOs?
- What’s your process for a rolling upgrade without disrupting ordering and latency?

## References
- Kafka Ops docs (disk/segment tuning, ISR); RabbitMQ HA queues; Pulsar tiered storage ops; Cloud provider managed messaging SLO docs
