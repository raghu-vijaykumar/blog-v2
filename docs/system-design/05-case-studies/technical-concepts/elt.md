+++
title= "ELT System"
tags = [ "system-design", "software-architecture", "interview", "elt" ]
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
weight= 29
bookFlatSection= true
+++

# Design ELT Data Pipeline

## Problem Statement

An ELT (Extract, Load, Transform) data pipeline system extracts data from diverse sources, loads it into a staging area, and transforms it for analysis. The system must handle 10TB daily data volumes with real-time updates for critical data, ensuring low-latency processing, high reliability, and compliance with security standards. It supports organizations in building scalable data lakes or warehouses for business intelligence.

*Assumed scale: 10TB/day, 1k concurrent pipelines, 99.9% uptime, end-to-end latency `<2` hours for batch.*

## Requirements

### Functional Requirements

- Extract data from multiple sources: relational databases, APIs, flat files, and streaming sources.
- Load raw data into a staging area with change data capture (CDC) for incremental loads.
- Transform data using SQL-based operations (joins, aggregations, cleansing) in the data lake/warehouse.
- Support both batch (daily) and near-real-time processing for critical pipelines.
- Provide data lineage tracking and pipeline monitoring/dashboard.
- Enable data quality checks and error handling.

### Non-Functional Requirements

- High throughput: Process 10TB data sets within 24 hours.
- Low latency: `<2` hours for batch jobs, `<30` minutes for real-time critical data.
- Scalability: Handle data growth from 10TB to 100TB with linear resource addition.
- Reliability: 99.9% uptime with automatic retry and failure recovery.
- Fault tolerance: Continue processing during node failures.
- Security: Encrypt data at rest/transit, access controls, GDPR compliance.

## Key Constraints & Assumptions

- Data sources: 100+ diverse sources including RDBMS (MySQL, PostgreSQL), APIs, CSV/JSON files, Kafka streams.
- Peak load: 10TB daily volume, growing at 50% YoY; support up to 1k concurrent pipelines.
- Network bandwidth: 10Gbps inter-DC links, 1Gbps per extraction node.
- SLA: 99.95% success rate per pipeline, `<2%` data loss tolerance.
- Cost: Cloud-optimized (AWS/GCP) with spot instances for non-critical workloads.
- Assumption: ELT preferred over ETL due to cheaper storage and schema-on-read flexibility.

## High-Level Design

The architecture consists of source connectors for extraction, staging storage for raw data, compute engines for transformation, and orchestration for workflow management. Raw data is extract-load-first, then transformed in-place using distributed processing.

Components and roles:
- **Source Connectors**: Agents that extract data via JDBC, REST, or file protocols.
- **Staging Area**: Distributed storage (e.g., S3/ADLS) for raw data with metadata.
- **Compute Engine**: Distributed processing cluster for transformations (e.g., Spark).
- **Target Storage**: Data lake/warehouse (Delta Lake, Snowflake) for transformed data.
- **Orchestration Layer**: Scheduler and workflow manager (Airflow) for pipeline orchestration.
- **Monitoring & Metadata Store**: Tracks pipeline status, lineage, and metrics.

Architecture diagram:

```mermaid
graph TD
    Sources[Data Sources: DB, APIs, Files] --> Connectors[Source Connectors]
    Connectors --> Staging[Staging Area: S3/ADLS]
    Staging --> Compute[Compute Engine: Spark]
    Compute --> Target[Target: Data Lake/Warehouse]
    
    Orchestrator[Orchestrator: Airflow] --> Connectors
    Orchestrator --> Compute
    Orchestrator --> Monitor[Monitoring & Logging]
    Monitor --> Alert[Alerts & Dashboards]
    
    subgraph Security
        Encryption[Data Encryption]
        Auth[Authentication]
    end
    
    Sources --> Auth
    Staging --> Encryption
```

## Data Model

Key entities:
- **Pipeline**: ID, name, schedule, source/target configs, status.
- **Dataset**: Raw tables in staging, metadata (schema, partitioning).
- **Transformed Table**: Views/queries on staging data for final schemas.

Storage choice: Cloud object storage (S3) for staging (cheap, scalable up to unlimited). Relational DB (PostgreSQL) or NoSQL (DynamoDB) for metadata. Delta tables in lakehouse for targeted (ACID updates, time travel).

Sketch:

```
Pipelines Table:
- pipeline_id (PK)
- name
- schedule
- source_config (JSON)
- transform_queries (array)
- status

Datasets Table:
- dataset_id
- pipeline_id
- path (S3 URI)
- schema (JSON)
- last_updated

Transform Jobs Table:
- job_id
- pipeline_id
- query
- execution_time
- status
```

## API Design

Core endpoints:

- `POST /pipelines` - Create or update pipeline.
  - Request: `{"name": "sales_pipe", "source": {"type": "db", "conn": "..."}}`
  - Response: `{"pipeline_id": "pid123", "status": "created"}`

- `GET /pipelines/{id}/status` - Get pipeline execution status.
  - Response: `{"status": "running", "progress": 75, "errors": []}`

- `POST /pipelines/{id}/run` - Trigger manual pipeline run.
  - Request: `{"incremental": true}`
  - Response: `{"run_id": "run456", "status": "queued"}`

- `GET /data/lineage/{dataset}` - Retrieve data lineage.
  - Response: `{"upstream": ["source1"], "downstream": ["tableA", "tableB"]}`

## Detailed Design

- **Source Connectors**: Custom agents or frameworks like Debezium (for CDC) or Singer (for various sources). Handle schema discovery, incremental extracts, and error retries.

- **Staging Area**: Object storage with partitioning (date-based) for efficient scans. Use Parquet/ORC for columnar format generation during load for faster transformations.

- **Compute Engine**: Apache Spark on Databricks/AWS EMR for distributed processing. Supports SQL and Python transformations with auto-scaling clusters.

- **Target Storage**: Lakehouse approach (Delta Lake) for schema enforcement, versioning, and unified analytics. Avoids expensive data movement.

- **Orchestration**: Apache Airflow for DAG definition, with sensors for upstream completion. Supports parallel execution and dependency management.

- **Monitoring**: Prometheus + Grafana for metrics (throughput, latency). Honeycomb or ELK for tracing and logs.

Technology choices:
- Connectors: Debezium for Kafka-based streaming; prefer over custom for battle-tested reliability.
- Compute: Spark vs. Presto – Spark better for heavy ETL; Presto for ad-hoc queries.
- Storage: S3/ADLS vs. HDFS – Cloud storage cheaper and elastic; HDFS for on-prem.

Workflow: Source -> Extract via CDC/change logs -> Load to staging (parquet) -> Transform in Spark -> Load to delta tables -> Consume via BI tools.

## Scalability & Bottlenecks

Horizontal scaling: Add more connector/compute nodes via auto-scaling groups. Partition data by date/hash for parallel processing.

Data sharding: Datasets partitioned by tenant/region to distribute load.

Caching: Intermediate results in memory/distributed cache (Alluxio) for repeated queries.

Replication: Multi-AZ storage for 99.999% durability.

Bottlenecks: Network I/O during extraction (limit: 1Gbps/node); mitigate with batching and compression. Compute skew on unbalanced partitions; use dynamic allocation. Storage limits: 100TB -> 1PB via sharding.

Scaling: From 10TB to 100TB – add nodes linearly; optimize with columnar formats (10x faster queries).

## Trade-offs & Alternatives

- **ELT vs. ETL**: ELT loads first, transforms later in warehouse (cheaper storage, flexible schemas but higher compute cost). ETL transforms upfront (network-friendly, but risky for schema changes).
- **Batch vs. Real-time**: Batch simplifies processing but delays insights; real-time adds complexity (streaming infra) for immediate needs.
- **Storage Choice**: Object store (S3) vs. HDFS – S3 cheaper and serverless but higher latency; HDFS faster locally but managed overhead.
- **Compute Framework**: Spark vs. Flink – Spark better for batch, Flink for stream; trade-off between maturity and low-latency.
- **Orchestration**: Airflow vs. Prefect – Airflow free and extensible; Prefect more user-friendly but proprietary.
- **Target Storage**: Data Lake vs. Warehouse – Lake flexible (schema-on-read, cheaper); Warehouse ACID but rigid (schema-on-write).

## Future Improvements

- Integrate streaming (Kafka Connect) for true real-time pipelines.
- Add ML feature engineering directly in the pipeline.
- Implement automated data quality monitoring with alerts.
- Support multi-cloud deployments for failover.
- Add metadata-driven pipeline generation for self-service.
- Optimize for edge computing with local transformation.

## Interview Talking Points

1. **ELT vs. ETL Trade-off**: ELT leverages cheap storage for large volumes but requires powerful compute; ETL transforms early, reducing downstream load but risking cost overruns on schema changes.
2. **Scalability via Parallelism**: Partition data and use distributed compute (Spark) to scale from TB to PB; avoid single-node bottlenecks with auto-sharding.
3. **Handling Large Volumes**: Use columnar formats (Parquet) for 10x faster queries; combine with partitioning for efficient scans and transformations.
4. **Reliability in Pipelines**: Implement idempotent operations and retry logic; track lineage for debugging failures in complex DAGs.
5. **Security Balance**: Encryption adds overhead but necessary for compliance; use short-term tokens for access to minimize credential exposure.
6. **Cost Optimization**: Cloud storage and spot instances cut costs 50%; trade-off with potential instance interruptions requiring careful state management.
7. **Real-time Extensions**: Batch for simplicity, stream for speed; Kafka connectors bridge hybrid models efficiently.
8. **Evolution Path**: Start with Airflow for orchestration, add Spark for compute; move to lakehouse for unified analytics as complexity grows.
