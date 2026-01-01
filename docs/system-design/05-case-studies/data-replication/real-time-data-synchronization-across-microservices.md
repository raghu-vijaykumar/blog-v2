---
title: "Real-Time Data Synchronization Across Microservices"
description: "System design example for Real-Time Data Synchronization Across Microservices"
---

# Real-Time Data Synchronization Across Microservices

## Problem Statement

Design a scalable system to synchronize data changes in real-time across multiple microservices in a distributed architecture. The system must ensure eventual consistency, handle high throughput data updates, support fault tolerance, and minimize data staleness while avoiding tight coupling between services. This is critical for modern applications where multiple services need to react to data changes simultaneously, such as updating user profiles across authentication, billing, and notification services.

## Requirements

### Functional Requirements
- **Data Synchronization**: Propagate data changes from source services to dependent services in real-time
- **Event Ordering**: Maintain causal ordering of related data changes to prevent consistency anomalies
- **Conflict Resolution**: Handle concurrent updates and resolve conflicts automatically or through configurable strategies
- **Schema Evolution**: Support backward-compatible schema changes without breaking synchronization
- **Subscription Management**: Allow services to subscribe to specific data change events based on filters
- **Audit Trail**: Provide mechanisms to track data change history for debugging and compliance

### Non-Functional Requirements
- **Low Latency**: Sub-second propagation of data changes for time-sensitive operations
- **High Availability**: 99.99% uptime with automatic failover and recovery
- **Scalability**: Support for 100K+ concurrent connections and millions of events per minute
- **Fault Tolerance**: Graceful degradation during partial system failures
- **Durability**: Guaranteed delivery of data changes with exactly-once semantics where possible
- **Observability**: Comprehensive monitoring, metrics, and alerting capabilities

## Key Constraints & Assumptions

- *Assumption*: Microservices are deployed in Kubernetes clusters across multiple availability zones
- *Assumption*: Data changes originate from primary databases (SQL/NoSQL) with write-ahead logging capabilities
- *Assumption*: Network latency between services is under 50ms within the same region
- *Assumption*: Data models are versioned with schema evolution strategies in place
- *Constraint*: No shared database access between microservices (enforces loose coupling)
- *Constraint*: Events must be processed in causal order to prevent logical inconsistencies

## High-Level Design

The proposed architecture leverages Change Data Capture (CDC), event streaming, and distributed messaging to synchronize data across microservices. Services publish domain events to a central event bus, which are then consumed by interested subscribers with appropriate filtering and transformation.

```
graph TB
    A[Microservice A<br/>Database] --> B[CDC Agent]
    C[Microservice B<br/>Database] --> D[CDC Agent]
    E[Microservice C<br/>Database] --> F[CDC Agent]

    B --> G[Event Publisher]
    D --> G
    F --> G

    G --> H[Event Stream<br/>Apache Kafka]
    H --> I[Event Processor]

    I --> J[Change Router]
    J --> K[Subscriber Registry]

    J --> L[Microservice X<br/>Consumer]
    J --> M[Microservice Y<br/>Consumer]
    J --> N[Microservice Z<br/>Consumer]

    O[Global Cache<br/>Redis Cluster] --> L
    O --> M
    O --> N

    L --> P[(Local Store)]
    M --> Q[(Local Store)]
    N --> R[(Local Store)]
```

## Data Model

### Domain Events
```json
{
  "eventId": "uuid",
  "eventType": "USER_PROFILE_UPDATED",
  "aggregateId": "user-123",
  "aggregateType": "User",
  "version": 1,
  "timestamp": "2024-01-01T10:00:00Z",
  "payload": {
    "before": { "email": "old@example.com" },
    "after": { "email": "new@example.com", "lastUpdated": "2024-01-01T10:00:00Z" }
  },
  "metadata": {
    "source": "auth-service",
    "causationId": "request-456",
    "correlationId": "session-789"
  }
}
```

### Subscriber Configuration
```json
{
  "subscriberId": "billing-service",
  "filters": [
    {
      "eventType": ["USER_PROFILE_UPDATED", "ACCOUNT_STATUS_CHANGED"],
      "conditions": [
        { "field": "aggregateType", "op": "eq", "value": "User" }
      ]
    }
  ],
  "transformation": "billing-mapping.js",
  "deliveryGuarantee": "AT_LEAST_ONCE"
}
```

## API Design

### Event Publishing API
```http
POST /events/publish
Content-Type: application/json

{
  "events": [
    {
      "eventType": "ORDER_CREATED",
      "aggregateId": "order-123",
      "aggregateType": "Order",
      "payload": { "userId": "user-456", "amount": 99.99 }
    }
  ]
}

Response: 202 Accepted
```

### Subscriber Management API
```http
POST /subscriptions
{
  "subscriberId": "notification-service",
  "eventTypes": ["USER_REGISTERED", "USER_PROFILE_UPDATED"],
  "webhookUrl": "https://notification-service.com/webhook"
}
```

### Event Consumption API
Services consume events via Kafka consumer groups or webhook delivery.

## Detailed Design

### Core Components

- **CDC Agents**: Deployed alongside each microservice database, capturing change events using tools like Debezium (for SQL) or Apache Pulsar (for NoSQL)
- **Event Publisher**: Serializes database changes into standardized domain events, adding correlation IDs and version information
- **Event Stream**: Apache Kafka or Redpanda for reliable, ordered event delivery with partitioning by aggregate ID
- **Event Processor**: Stateless workers that filter, transform, and route events to appropriate subscribers
- **Subscriber Registry**: Distributed configuration store (Consul/ETCD) maintaining subscription rules and routing tables
- **Global Cache**: Redis cluster for temporary storage of frequently accessed synchronized data, reducing database load
- **Dead Letter Queue**: For handling failed deliveries and supporting retry logic with exponential backoff

### Technology Choices Rationale

- **Kafka**: Provides exactly-once delivery semantics with consumer group rebalancing and horizontal scaling
- **CDC with Debezium**: Non-intrusive change capture without modifying application code
- **gRPC/Webhooks**: High-performance pub/sub with fallback to HTTP for language-agnostic integration
- **Redis Cache**: Sub-millisecond reads for hot data paths with LRU eviction

## Scalability & Bottlenecks

### Scaling Strategies
- **Horizontal Partitioning**: Kafka topics partitioned by aggregate ID to maintain order per entity
- **Consumer Group Scaling**: Multiple instances per subscriber service, each processing disjoint partitions
- **Region-based Replication**: Kafka MirrorMaker or Confluent Replicator for cross-region data synchronization
- **Database Sharding**: Event stores partitioned by time ranges or aggregate types

### Potential Bottlenecks
- **Publish Rate Limits**: Cap per-producer rates to prevent broker overload (target: 1M events/minute per topic)
- **Consumer Lag**: Monitor lag metrics, auto-scale consumer groups when lag exceeds thresholds
- **Event Store Growth**: Implement time-based retention policies and compaction for old events
- **Network Saturation**: Use protocol buffers for efficient serialization and compression

## Trade-offs & Alternatives

### Eventual vs Strong Consistency
- **Trade-off**: Eventual consistency allows better performance/scalability but requires handling of temporary inconsistencies
- **Alternative**: Two-phase commit protocols for strong consistency at cost of throughput and availability

### Push vs Pull Models
- **Trade-off**: Push model (webhooks) offers lower latency but increases coupling; pull model (API polling) is more resilient but introduces latency
- **Alternative**: Hybrid approach with push notifications triggering pull-based synchronization

### Centralized vs Decentralized Event Processing
- **Trade-off**: Centralized routing simplifies management but creates single point of failure; decentralized (service mesh) increases complexity
- **Alternative**: Event-driven architecture with sidecars (Istio/Envoy) for service-to-service communication

## Future Improvements

- **Event Sourcing**: Store full event history for temporal queries and system reconstruction
- **CQRS Pattern**: Separate read/write models for optimized query performance
- **Global Transaction IDs**: Implement saga patterns for distributed transaction coordination
- **Machine Learning Integration**: Anomaly detection for event patterns and predictive scaling
- **Blockchain Integration**: Immutable audit trails for regulatory compliance (GDPR, SOX)

## Interview Talking Points
- **Explain the CAP theorem trade-offs**: How eventual consistency enables high availability and partition tolerance at the cost of immediate consistency
- **Design event schema evolution**: Versioned events with upcasting/downcasting to handle backward compatibility
- **Handle out-of-order events**: Use event versioning and idempotent processing to maintain correctness despite network partitions
- **Scale beyond Kafka limits**: Implement hierarchical event routing with regional aggregators and global coordination
- **Debug synchronization issues**: Correlation IDs for tracing events across service boundaries and distributed logging
- **Prevent event storms**: Implement circuit breakers and rate limiters to protect downstream services from cascade failures
- **Balance throughput vs latency**: Use batching and compression for high-throughput scenarios while maintaining sub-second delivery for critical paths
- **Ensure data integrity**: Implement checksums and sequence numbers to detect and recover from data corruption during transmission
- **Migration strategy**: Zero-downtime deployment with dual-writes and gradual cutover to minimize disruption during system evolution
