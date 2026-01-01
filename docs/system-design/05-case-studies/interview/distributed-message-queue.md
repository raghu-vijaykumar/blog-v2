+++
title= "Distributed Message Queue"
tags = [ "system-design", "software-architecture", "interview", "distributed-message-queue" ]
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
weight= 18
bookFlatSection= true
+++

# Design Distributed Message Queue

## Problem Statement

A distributed message queue system enables asynchronous communication between producers and consumers, allowing decoupling of applications. The system must handle high throughput, ensure message durability, provide scalability, and support exactly-once delivery semantics while maintaining low latency. It facilitates communication in microservices architectures, IoT data processing, and event-driven systems.

*Assumed scale: 100M messages/day, 1M requests/sec peak, data retention of 7 days, target latency under 100ms.*

## Requirements

### Functional Requirements

- Send messages to queues with optional priority and expiration.
- Receive messages from queues with configurable visibility timeouts.
- Create, configure, and delete queues.
- Support exactly-once delivery semantics to prevent duplicates.
- Provide topic-based pub/sub in addition to queue-based messaging.
- Implement access control with authentication and authorization.

### Non-Functional Requirements

- High throughput: Handle 1M requests/second.
- Low latency: `<100ms` for send/receive operations.
- High availability: 99.99% uptime across multiple data centers.
- Durability: Messages persisted even during failures.
- Scalability: Linear scaling with additional nodes.
- Fault tolerance: Survive node failures without data loss.

## Key Constraints & Assumptions

- Global user base with 1B daily messages, growing to 10B in 2 years.
- Queue size limits: 10M messages per queue; retention period of 7 days
- Network bandwidth: Assume 1Gbps per node; cross-DC latency ~50ms.
- SLA: 99.9% availability, 95% of ops `<50ms` latency.
- Security: End-to-end encryption, compliance with GDPR/HIPAA.
- Cost constraints: Optimize for cloud deployment (AWS/EC2, storage costs).
- Assumption: Primary storage on local SSDs with replication to 3 nodes.

## High-Level Design

The architecture consists of clients (producers/consumers), a load balancer tier, FrontEnd services for request handling, Metadata services for queue information, and Backend services for message storage and retrieval. Messages flow from producers through the stack, stored on disks with replication, and consumed by pulling or pushed to subscribers.

Components and roles:
- **Virtual IP (VIP)**: Single entry point for DNS resolution.
- **Load Balancer**: Distributes requests to FrontEnd nodes.
- **FrontEnd Web Service**: Handles API ingress, includes rate limiting and caching.
- **Metadata Service**: Manages queue/topic metadata and partitions.
- **Backend Web Service**: Manages partitions, message persistence, and leader election.

Include architecture diagram code block (Mermaid or PlantUML):

```mermaid
graph TD
    Client[Producers/Consumers] --> VIP[Virtual IP]
    VIP --> LB[Load Balancer]
    LB --> FE[FrontEnd Service Tier]
    FE --> MS[Metadata Service]
    MS --> BE[Backend Service Cluster]
    BE --> Disk1[Local SSD Storage]
    BE --> Disk2[Replicated Storage]
    BE --> Disk3[Backup Storage]

    subgraph Components
        Leader[Partition Leader]
        Followers[Followers for Replication]
        Manager[Cluster Manager]
    end

    BE --> Leader
    Leader --> Followers
    Manager --> Cluster[Partition Assignment]
```

## Data Model

Key entities:

- **Queue/Topic**: Metadata (ID, name, partition count, retention policy).
- **Message**: ID (UUID), payload (up to 256KB), timestamp, expiration, priority.
- **Partition**: Segment of a queue, consists of messages in order; uses offsets for ordering.
- **Consumer Group**: For topic consumption, tracks offsets per group.

Storage choice: Local SSDs for primary storage (high throughput, ~500MB/s read/write). Zookeeper/ETCD for metadata coordination. Replication ensures durability.

Sketch:

```
Queue Table:
- queue_id (PK)
- name
- partitions [list of partition_ids]
- retention_days

Message Table (per partition):
- message_id (PK)
- queue_id
- partition_id
- offset
- payload (blob)
- timestamp
- status (sent/acked)
```

## API Design

Core endpoints:

- `POST /queues/{queue_name}/send` - Send message.
  - Request: `{"payload": "msg", "priority": 1}`
  - Response: `{"message_id": "abc123", "status": "sent"}`

- `GET /queues/{queue_name}/receive` - Pull message(s).
  - Request: `{"batch_size": 10, "visibility_timeout": 30}`
  - Response: `[{"message_id": "abc", "payload": "msg"}, ...]`

- `POST /queues/{queue_name}/{message_id}/ack` - Acknowledge message.
  - Request: `{}` (empty body)
  - Response: `{"status": "acknowledged"}`

- `PUT /queues/{queue_name}` - Create/update queue config.
  - Request: `{"partitions": 3, "retention": 7}`

## Detailed Design

- **Frontend Service**: Stateless nodes for request routing, authentication (OAuth/JWT), rate limiting (token bucket algorithm per user/IP), SSL termination. Caches queue metadata and user ACLs in Redis.

- **Metadata Service**: Stores queue configs in sharded key-value store (e.g., Cassandra). Uses Zookeeper for leader election and partition assignments. Sharding by queue ID hash for scalability.

- **Backend Service**: Leader-based per partition; uses Raft/Paxos for consensus. Messages stored in append-only logs on local SSDs. Replication factor 3: synchronous for durability, asynchronous for performance. Lazy deletion with background garbage collection.

- **Message Queue Workflow**: Producer sends message; Frontend routes to backend leader; message appended to log, replicated; consumer pulls, ack removes from queue (delayed deletion).

Technology choices:
- FrontEnd: Go/Java due to high concurrency.
- Backend: Custom log-based storage inspired by Kafka (efficient appends).
- Message Queue: Kafka/RabbitMQ for admin cmds; prefer Kafka for distributed setup (partitions, replication).
- Alternatives: RabbitMQ for simpler setups (uses AMQP, has built-in exchanges/queues).

## Scalability & Bottlenecks

Horizontal scaling: Add more nodes to FrontEnd/Backend tiers. Partitions enable parallel processing (sharding by queue/partition key). Load balancing via consistent hashing.

Replication: 3x synchronous across racks for fault tolerance. Read replicas for hot partitions.

Caching: Queue metadata in FrontEnd, hot messages in memory.

Bottlenecks: Disk I/O limits throughput (~1M ops/sec per disk); solution: SSD stripes. Network bandwidth for replication; use compression. Leader bottlenecks: use partition-based load distribution.

Scaling from 100k to 1M ops/sec: Add partitions and nodes; auto-sharding via metadata service.

## Trade-offs & Alternatives

- **Storage Choice**: Local SSDs vs. Network-attached storage – SSDs offer higher throughput (500MB/s vs. 100MB/s) but require replication for durability.
- **Replication**: Sync vs. async – Sync ensures consistency but increases latency; async improves performance at risk of data loss on failures.
- **Delivery Model**: Pull (Kafka) vs. push (RabbitMQ/WebSocket) – Pull is simpler and more reliable but consumes more client-side resources; push is efficient but harder to scale and can overload receivers.
- **Guarantees**: At-least-once vs. exactly-once – Exactly-once requires transaction logs and idempotency checks, adding complexity; at-least-once is simpler but may have duplicates.
- **Partitioning Strategy**: Based on queues (Kafka) vs. hash-partitioning (RabbitMQ) – Queue-based is predictable but can cause hotspots; hash-based distributes evenly but may break ordering.
- **Technology Stack**: Kafka vs. custom – Kafka is battle-tested with high throughput; custom allows tailored optimization but requires maintenance.

## Future Improvements

- Implement global replication for multi-region failover.
- Add analytics layer for message throughput dashboards.
- Support message tags and filtering for advanced routing.
- Integrate with stream processing (e.g., Kafka Streams) for real-time analytics.
- Optimize for edge computing with geographically distributed brokers.

## Interview Talking Points

1. **Throughput vs. Latency Trade-off**: Synchronous replication ensures durability but increases latency; asynchronous allows higher throughput at cost of potential data loss.
2. **Scalability via Partitioning**: Break queues into partitions for parallel processing, using leaders for writes and followers for reads.
3. **Exactly-Once Delivery Challenge**: Implemented via idempotent operations and transactional logs, but adds overhead compared to at-least-once.
4. **Pull vs. Push Models**: Pull is consumer-controlled (good for load management) but wasteful on low-traffic queues; push is efficient but can overwhelm slow consumers.
5. **Storage Optimization**: Local SSDs provide high-speed access, but replication (3x) ensures durability; balance with compression to reduce storage costs.
6. **Fault Tolerance**: Leader election (Raft) handles failures, with cross-DC replication preventing single-point failures.
7. **Security**: SSL for transport, encryption at rest; OAuth for auth; discuss OWASP top 10 for message brokers.
8. **Monitoring Trade-offs**: Collect metrics (throughput, lag) but avoid overwhelming the system with too granular logging.
9. **Cost Efficiency**: Optimize for cloud – use spot instances for non-critical components; auto-scaling prevents over-provisioning.
10. **Evolution**: Start simple (single-node) and add complexity (multi-partition, replication) as scale grows.
