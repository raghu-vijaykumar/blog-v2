+++
title= "Metrics Monitoring and Alerting"
tags = [ "system-design", "software-architecture", "interview", "metrics-monitoring-and-alerting" ]
author = "Me"
showToc = true
TocOpen = false
draft = false
hidemeta = false
comments = false
disableShare = false
disableHLJS = false
hideSummary = false
searchHidden = true
ShowReadingTime = true
ShowBreadCrumbs = true
ShowPostNavLinks = true
ShowWordCount = true
ShowRssButtonInSectionTermList = true
UseHugoToc = true
weight= 19
bookFlatSection= true
+++

# Design Metrics Monitoring and Alerting System

## Problem Statement
Design a scalable metrics monitoring and alerting system that collects, stores, and analyzes operational metrics from large-scale infrastructure, detects anomalies, and triggers alerts via multiple channels. The system must handle high-volume time-series data with low latency for queries and dashboards while ensuring reliability for critical alerts.

## Requirements

### Functional Requirements
- Collect operational metrics (e.g., CPU load, memory usage, request counts, message queue lengths) from servers, applications, and services.
- Store metrics as time-series data with configurable retention policies.
- Query metrics for aggregation, analysis, and visualization (e.g., averages, percentiles over time ranges).
- Detect anomalies based on predefined rules and trigger alerts to channels like email, SMS, PagerDuty, and webhooks.
- Visualize metrics via dashboards showing graphs and charts.

### Non-Functional Requirements
- **Scalability**: Handle 10 million metrics per second from 100,000 servers across 1,000 server pools. *Assumption: System must scale horizontally to support growth to 200,000 servers.*
- **Latency**: Query responses within 1-2 seconds for dashboards; alert detection within 30 seconds of condition trigger.
- **Reliability**: 99.9% uptime with at-least-once alert delivery; tolerate occasional data loss but avoid missing critical alerts.
- **Durability**: Retain data for 1 year with progressive roll-up (raw for 7 days, 1-minute resolution for 30 days, 1-hour resolution beyond 30 days).
- **Extensibility**: Easy integration of new metric sources, alert channels, and visualization tools.

## Key Constraints & Assumptions
- **Infrastructure Scale**: 100 million daily active users driving traffic; 1,000 server pools × 100 machines × ~100 metrics/machine = ~10 million metrics base. *Assumption: Peak load reaches 50 million metrics per second during traffic spikes.*
- **Data Volume**: 1 petabyte raw data annually; storage optimized via compression and down-sampling.
- **SLA**: 99.95% availability for data ingestion; `<5-`minute alert notification delay for critical issues.
- **Alert Load**: 1,000 active alert rules; 100 alerts per minute at peak.
- **No Logs or Tracing**: Focus on metrics only; out-of-scope for logs aggregation or distributed tracing.
- **Deployment**: Internal use for a large tech company with existing infra (Kubernetes, load balancers).

## High-Level Design
The system comprises six core components: metric sources, collectors, messaging queue, time-series database, query service, alerting system, and visualization layer. Metrics flow from sources via collectors to a queue for ingestion into the database, with parallel processing for alerts and queries.

```mermaid
flowchart TD
    A[Metric Sources <br/> (Servers, Apps, Queues)] -->|Push/Pull| B[Metric Collectors]
    B -->|Batch| C[Kafka/Queue]
    C -->|Stream| D[Time-Series DB <br/> (InfluxDB/Prometheus)]
    D --> E[Query Service]
    E --> F[Visualization <br/> (Grafana)]
    D -->|Stream| G[Alerting System]
    G -->|Notifications| H[Channels <br/> (Email, SMS, etc.)]

    subgraph "Data Flow"
        A --> B
        B --> C
        C --> D
    end

    subgraph "User Interaction"
        E
        F
        G --> H
    end
```

## Data Model
Metrics are stored as time-series with metric names, tags (dimensions), timestamps, and values.

### Key Entities
- **Time-Series**: Identified by `<metric_name> {<label1=value1, label2=value2>} <timestamp> <value>`
  - Example: `cpu_load{host=server01, region=us-west} 1633027200 85.5`
- **Labels/Tags**: Key-value pairs for dimensional querying (e.g., host, service, region). *Assumption: Cardinality limited to `<10,`000 unique combinations per metric to avoid index explosion.*

### Storage Schema Sketch
Time-series databases use optimized storage:
- Writes: High-frequency (1-10 seconds).
- Reads: Aggregated queries (e.g., `avg(cpu_load) by host over 1h`).
- Choice: InfluxDB or Prometheus with automatic retention and roll-up.

## API Design
No full REST API needed; interaction via query interfaces. Key endpoints:

### Query Service API (Internal)
- `GET /query?metric=cpu_load&start=1633027200&end=1633027260&aggregation=avg&group_by=host`
  - Response: JSON array of time-series points.
- `POST /rules/evaluate` (for alerting: sends rule and returns matched conditions).

### Ingestion Endpoint
- `POST /ingest` (from collectors): Batch time-series data in line protocol format.

## Detailed Design
### Component Breakdown
- **Metric Collectors**: Agents installed on servers collect local metrics (push model) or scrape endpoints (pull model) every 10-30 seconds. Use consistent hashing to distribute collection across multiple collectors. *Trade-off: Push favored for network flexibility; pull for reliability.*
- **Messaging Queue (Kafka)**: Buffers ingested data; partitions by metric name for ordered processing. Handles 50 MB/s peak with retention for 24 hours.
- **Time-Series Database**: Stores compressed data with indices on labels. Supports down-sampling (7d raw → 1m roll-up). Uses in-memory cache for recent data (`<26h`).
- **Query Service**: REST interface with caching (Redis) for frequent queries. Delegates to DB for time-series operations (e.g., exponential moving averages).
- **Alerting System**: Evaluates rules (YAML-configured) every 15-60 seconds via query service. Deduplicates alerts, uses KV store (Cassandra) for state. Publishes to Kafka for delivery.
- **Visualization**: Grafana plugins query data; off-the-shelf to avoid complexity.

## Scalability & Bottlenecks
- **Horizontal Scaling**: Collectors and DB autoscale via Kubernetes; Kafka partitions data.
- **Sharding**: DB shards by metric/time range; aggregator nodes reduce query load.
- **Caching**: Redis for hot queries; LRU eviction for visualization.
- **Bottlenecks**: High label cardinality causes slow queries; mitigated via normalization. Queue spikes during outages:text overflow.

## Trade-offs & Alternatives
- **Push vs. Pull Collection**: Push offers flexibility (network, auth via whitelist) but risks fake data; pull guarantees authenticity but requires open endpoints/network complexity.
- **SQL vs. NoSQL DB**: Time-series DBs (InfluxDB) preferred for native aggregation over DynamoDB/Cassandra, which require manual schema optimization and slower queries.
- **Custom Query Service vs. Direct DB Integration**: Adds decoupling but latency; off-the-shelf tools (Grafana) often integrate directly.
- **Kafka vs. Direct Ingestion**: Prevents data loss but adds operational overhead; alternative like Gorilla for simplified high-scale ingestion.
- **Centralized vs. Federated Alerts**: Central system for global rules; federated for regional isolation but increases config complexity.

## Future Improvements
- Support business metrics (e.g., revenue trends).
- Integrate ML for proactive anomaly detection.
- Use cold storage (S3) for archival data beyond 1 year.
- Add multi-datacenter replication for geo-redundancy.

## Interview Talking Points
1. **Data Model & Scale**: Time-series challenges (write-heavy, spiky queries); chose TSDB over NoSQL for efficient indexing.
2. **Collection Trade-offs**: Push preferred for scale/spikes; pull for health checks/authenticity.
3. **Queue Benefits**: Kafka decouples collection/storage; handles peaks but operational cost.
4. **Alert Reliability**: At-Least-Once via KV store; deduplication prevents spam.
5. **Scaling Bottlenecks**: Down-sampling reduces storage; consistent hashing balances load.
6. **Tech Choices**: InfluxDB for OOTB analytics; Grafana to avoid custom viz complexity.
