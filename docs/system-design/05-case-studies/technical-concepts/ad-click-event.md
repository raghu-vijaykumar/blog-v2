+++
title= "Ad Click Event Aggregation"
tags = [ "system-design", "software-architecture", "interview", "ad-click-event" ]
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
weight= 20
bookFlatSection= true
+++

# Design Ad Click Event Aggregation

## Problem Statement
Design a system at Facebook/Google scale to aggregate ad click events in near real-time for billing, reporting, and real-time bidding (RTB) optimization. The core goals are to provide accurate click counts per ad and ranking of top-clicked ads, handling massive daily volumes while supporting flexible filtering.

## Requirements

### Functional Requirements
- Aggregate click counts for a specific ad_id over the last Y minutes.
- Return the top N most clicked ad_ids within the last M minutes, with aggregation occurring every minute.
- Support filtering aggregations by attributes like IP, user_id, or country.

### Non-Functional Requirements
- Data accuracy for billing and RTB purposes.
- Handle late-arriving or duplicate events.
- System resilience to partial failures.
- End-to-end latency of 2-5 minutes for aggregations.

## Key Constraints & Assumptions
- 1 billion ad clicks per day, growing 30% annually; 2 million total ads.
- Average QPS: 10k, peak: 50k.
- Event time used for aggregation (assumed from ad click timestamp).
- Daily raw storage: 100GB (0.1KB per event), monthly: 3TB (assumption for cold storage).
- SLAs: Aggregations within 5 minutes for 99% of events.

## High-Level Design
The system processes unbounded streams of ad click events using a streaming architecture decoupled via message queues. Events are ingested, aggregated per minute using a MapReduce-style DAG, and stored in a NoSQL database for fast queries. Components include:
- **Event Ingestion**: Producers send events to Kafka.
- **Aggregation Service**: MapReduce nodes (map/filter, aggregate/count, reduce/collect) process data in parallel.
- **Data Storage**: Separate stores for raw (Cassandra/S3) and aggregated data (Cassandra for reads).
- **Consumer APIs**: Expose aggregated results.

```mermaid
graph TD
    A[Event Producers] --> B[Kafka Queue 1: Raw Events]
    B --> C[Map Nodes: Filter/Transform]
    C --> D[Aggregate Nodes: Count per minute]
    D --> E[Reduce Nodes: Collect results]
    E --> F[Kafka Queue 2: Aggregated Counts/Top Ads]
    F --> G[Storage Layer: Raw & Aggregated Data]
    H[API Consumers] --> I[Dashboards/Reports]
```

## Data Model
**Raw Events Table** (Cassandra for writes/heavy ingest):
- ad_id (partition key)
- click_timestamp (clustering key)
- user_id, ip, country

**Aggregated Counts Table**:
- ad_id (partition key)
- click_minute (clustering key)
- filter_id, count

**Top Ads Table**:
- window_size (minutes)
- update_minute
- most_clicked_ads (JSON array)

Filtering dimensions: Pre-aggregate per filter (e.g., country) using star schema to enable fast queries.

## API Design (if relevant)

**Get Ad Click Count**:
```
GET /v1/ads/{ad_id}/aggregated_count
Params: from (start minute), to (end minute), filter_id
Response: { "ad_id": "ad001", "count": 150, "window": "202101010000-202101010059" }
```

**Get Top Clicked Ads**:
```
GET /v1/ads/popular_ads
Params: count (N), window (minutes), filter_id
Response: { "ads": ["ad001", "ad002"], "updated_at": "202101010002" }
```

## Detailed Design
Component-wise breakdown:
- **Ingest Layer**: Kafka decouples producers from consumers, handling high throughput (10k-50k QPS).
- **Aggregation Service**: Uses tumbling windows (1-minute fixed) for counts, sliding windows for top-N ranking. MapReduce DAG: Map node filters/transforms data, assigns to aggregates by hash(ad_id). Aggregate nodes count in-memory. Reduce nodes merge results, handle deduplication via distributed transactions.
- **Filtering**: Pre-compute aggregations per dimension (e.g., country) to avoid runtime filtering.
- **Storage**: Raw data in Cassandra or S3 (Parquet) for freshness/errors; aggregated in Cassandra for fast reads.

Assumptions: Use event time with watermarks to handle late events, capping latency at 5 minutes.

## Scalability & Bottlenecks
- **Message Queue Scaling**: Increase partitions preemptively; add consumers in groups for horizontal scaling. Partition by geography if needed (e.g., topic_na).
- **Aggregation Service**: Scale MapReduce DAG by adding nodes; use multi-threading or YARN for throughput. Handle hotspots via dynamic resource allocation (e.g., split overloads across nodes).
- **Database**: Cassandra's consistent hashing enables horizontal scaling without manual sharding. Auto-rebalances data on node additions.
- **Bottlenecks**: Hotspots (popular ads overload nodes); mitigated by global-local aggregation. Late events via watermarks; reconciled end-of-day.

## Trade-offs & Alternatives
- **Streaming vs. Batch**: Streaming provides near-real-time (2-5 min latency) but requires complex windowing/duplication handling. Batch offers accuracy but higher latency; use Kappa architecture to unify paths.
- **Event Time vs. Processing Time**: Event time for accuracy despite potential malicious timestamps; processing time more reliable but inaccurate for late events.
- **Delivery Guarantees**: Exactly-once for billing accuracy via atomic commits, increasing complexity over at-least-once.
- **Storage Choice**: NoSQL (Cassandra) scales writes better than RDBMS; raw data in Cassandra/S3 trades query speed for debuggability. Pre-aggregated star schema speeds reads but multiplies data volume.

## Future Improvements
- Adopt Kappa architecture fully to simplify dual-codebases in lambda.
- Implement end-of-day batch reconciliation for discrepancy detection.
- Use advanced watermarking or session windows for better late-event handling.
- Integrate OLAP tools like ClickHouse/Druid with Elasticsearch for faster querying without custom aggregation.

## Interview Talking Points
1. Scalability: How to scale Kafka, aggregation, and Cassandra independently for 30% YoY growth.
2. Accuracy Trade-off: Event time aggregation with watermarks vs. speed with processing time.
3. Deduplication: Distributed transactions for exactly-once guarantees in billing-critical system.
4. Filtering Efficiency: Pre-aggregation using dimensions to enable fast queries without runtime computation.
5. Fault Tolerance: Snapshots for state recovery in in-memory aggregation pipelines.
6. Hotspots: Dynamic resource allocation to prevent popular ad overloads in MapReduce.
7. Architecture Choice: Kappa vs. Lambda for balancing real-time and historical reprocessing.
8. Storage Hybrid: Raw cold storage for debugging vs. aggregated hot storage for fast reads.
