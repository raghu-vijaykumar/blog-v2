---
title: Operations, Observability, and Runbooks
description: SRE essentials for replication—capacity and lag SLOs, dashboards and alerts, maintenance procedures, backfill/throttling, and standard runbooks.
---

## Overview
Operational excellence in replicated systems hinges on tight observability of lag and throughput, proactive capacity planning, and ruthlessly rehearsed runbooks for failover, rebuilds, and backfills.

## What, Why, When (and when-not)
What
- Metrics, dashboards, alerts, and procedures that ensure replication meets durability/availability SLOs and avoids cascading failures during incidents and maintenance.

Why
- Replication silently drifting (lag) breaks freshness guarantees and DR posture; visibility and automation prevent surprises and speed recovery.

When
- Any production deployment using replication.

When-not
- N/A—observability is mandatory for reliable replication.

## Core concepts and metrics
Primary metrics
- Replication lag (time and LSN/index distance), apply throughput, WAL queue depth, ack distribution (sync/semi-sync participants), RPO estimator.

Capacity metrics
- Leader egress bandwidth, follower apply CPU/IO, snapshot/restore bandwidth, cross-region RTT/loss.

Health metrics
- Election/epoch churn, error rates on apply, conflicts detected/resolved (multi-primary/leaderless), read-fallback rate to leaders.

## Design decisions and trade-offs
- Aggressive alerting vs noise: alert on SLO-affecting thresholds (e.g., 5m lag for analytics, 500ms for RYW APIs); warn earlier via dashboards.
- Maintenance windows vs always-on: throttle backfills and compactions; prioritize serving traffic.

## Dashboards
- Overview: lag percentiles, leader egress, follower apply throughput, error budgets, RPO estimate.
- Replica detail: per-replica lag time, applied LSN, backpressure state, disk IO and fsync latency.
- Geo: inter-region RTT, packet loss, cross-region backlog.

## Alerts
- Lag > budget (time/LSN) for X minutes.
- Semi-sync acker down → risk to RPO; switch policy or reduce write rate.
- Apply error spike; WAL queue depth growing unchecked.
- Election churn > Y/hour; potential instability.

## Runbooks
Planned maintenance (minor version upgrade)
```text
1) Mark followers as upgrade candidates; drain traffic if they serve reads.
2) Upgrade one follower; verify replication catch-up and health.
3) Rotate upgrade across followers; then perform planned leader switchover.
4) Upgrade former leader; restore normal read routing.
```

Replica rebuild from snapshot
```text
1) Take base snapshot from leader/backup store.
2) Restore to new node; start WAL/CDC from snapshot position.
3) Backfill until applied >= leader; add to read pool; optionally reparent.
```

Backfill/res harding throttling
```text
1) Set target throughput (MB/s, rows/s) per donor; cap global concurrent donors.
2) Monitor p95 latency and lag; reduce throttle if SLOs degrade.
3) Use token-bucket or adaptive controller tied to latency budgets.
```

Incident: lag spike
```text
1) Identify scope: one replica vs many; region-wide vs local.
2) Switch affected read traffic to fresher replicas or leader.
3) Increase applier parallelism/IO where safe; reduce write load via rate limits.
4) If persistent, rebuild the outlier or investigate storage/network.
```

## Operational checklists
- Define SLOs: max lag (time/LSN), RPO targets, failover RTO.
- Provision bandwidth and IO to keep apply throughput > peak commit rate.
- Automate snapshot/restore and seeding; test quarterly.
- Protect leaders with backpressure; reject writes rather than accumulate unbounded lag.

## Examples
Example A (quantitative): Lag budget sizing
- Peak leader commit: 30k tx/s; average tx apply cost 200 μs → max single-thread apply ≈ 5k tx/s; need ≥ 6 parallel apply workers per follower to keep up with headroom.

Example B (architectural): Multi-region dashboard
- Per-region panels for leader egress, follower lag percentiles, RPO estimate, and WAN health. Drill-down links to replica detail and failover status.

## Edge cases and anti-patterns
- Ignoring fsync latency regressions after storage changes; always baseline and alert on storage KPIs.
- Letting backfills run unthrottled during peak hours; enforce windows or adaptive control.

## Interactions with adjacent topics
- [Availability & Fault Tolerance](../09-availability-and-fault-tolerance/): SLOs and budgets.
- [Observability](../11-observability/): metrics and tracing foundations.
- [Rate Limiting & Backpressure](../08-rate-limiting-and-backpressure/): controlling producers during lag.

## Production checklist
- Dashboards and alerts in place for lag, throughput, errors, elections.
- Documented runbooks for failover, rebuild, upgrade, and throttle control.
- Periodic DR drills with measured RPO/RTO.

## Interview framing checklist
- How do you size follower apply parallelism and IO to match leader throughput?
- What’s your response plan when replication lag exceeds budget?

## References
- Google SRE Book (overload, backpressure); vendor docs for Postgres/MySQL replication monitoring; Kafka MirrorMaker/Mirrormaker2 ops guides
