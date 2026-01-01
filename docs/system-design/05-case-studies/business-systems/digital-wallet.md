+++
title= "Digital Wallet"
tags = [ "system-design", "software-architecture", "interview", "digital-wallet" ]
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
weight= 26
bookFlatSection= true
+++

# Design Digital Wallet

## Problem Statement
Design a digital wallet service that enables secure and fast balance transfers between user accounts. The system must handle high transaction volumes while ensuring correctness, transactional integrity, and auditability through event replay.

## Requirements
### Functional Requirements
- Support balance transfers between two accounts.
- Provide idempotent transactions using transaction IDs.
- Retrieve historical account balances at any point in time.
- Ensure atomicity, consistency, isolation, and durability (ACID) for transactions.

### Non-Functional Requirements
- Process 1 million transactions per second (TPS).
- Achieve 99.99% uptime.
- Maintain low latency for transfers (`<100ms`).
- Support reproducibility of system state for auditing.

## Key Constraints & Assumptions
- Focus on intra-wallet transfers; external integrations like banks are out of scope.
- No foreign exchange support.
- Assume a user base of 100 million active users, with peak TPS of 1 million (each transfer involves two account updates).
- SLA: 99.99% availability, 100ms p50 latency, 1s p99 latency.
- Data retention: Historical events for 7 years.
- Security: Transactions require authentication; assume OAuth-based auth layer exists.

## High-Level Design
The system uses event sourcing with sharded Raft consensus groups for distributed, high-performance balance transfers. A Saga orchestrator ensures distributed transactions across shards.

- **Components**:
  - **Saga Coordinator**: Handles distributed transaction logic, coordinating try/confirm/cancel phases.
  - **Raft Groups**: Sharded replicas store event logs and state; leaders process commands.
  - **Command Queue**: FIFO queue (e.g., Kafka) for transfer requests.
  - **Event Store**: Append-only logs for events (transfers, balances).
  - **State Machines**: Apply events to update balances; separate read/write for CQRS.
  - **Reverse Proxy**: Batches requests and pushes real-time updates to clients.

Transaction Flow: User sends transfer → Saga Coordinator initiates TC/C → Commands sent to shards → Events generated and replicated → Balances updated → Response pushed.

```mermaid
graph TD
    A[User] --> B[Reverse Proxy]
    B --> C[Saga Coordinator]
    C --> D[Shard 1 Raft Group]
    C --> E[Shard 2 Raft Group]
    D --> F[Event Store 1]
    E --> G[Event Store 2]
    F --> H[State Machine 1]
    G --> I[State Machine 2]
    H --> J[Read State Machines]
    I --> J
    J --> B
```

## Data Model
- **Entities**:
  - **Account**: ID (string), Balance (decimal), Currency (string).
  - **Transaction**: ID (UUID), FromAccount, ToAccount, Amount, Currency, Status.
  - **Event**: SequenceID, AccountID, EventType (debit/credit), Amount, Timestamp.
- **Storage Choice**: Use RocksDB for high-write performance on events. Balances stored in memory with snapshots.
- **Schema Sketch**:
  - Accounts Table: PRIMARY KEY (account_id), balance DECIMAL(15,2), currency VARCHAR(3).
  - Events Table: PRIMARY KEY (sequence_id, account_id), event_type VARCHAR(10), amount DECIMAL(15,2), timestamp BIGINT.

## API Design
POST /v1/wallet/balance_transfer

**Request:**
```json
{
  "from_account": "12345",
  "to_account": "67890",
  "amount": "100.00",
  "currency": "USD",
  "transaction_id": "uuid-123"
}
```

**Response (Success):**
```json
{
  "status": "success",
  "transaction_id": "uuid-123"
}
```

**Response (Failure):**
```json
{
  "status": "failed",
  "transaction_id": "uuid-123",
  "error": "Insufficient funds"
}
```

- **Endpointsleichs**: Idempotency handled via transaction_id; duplicates return original response.

## Detailed Design
- **Caching Layer**: In-memory Redis sharded by account ID hash; Zookeeper manages configurations.
- **Database Sharding**: Accounts partitioned into Raft groups; hash-based routing (account_id % num_shards).
- **Message Queue**: Kafka for commands; append-only for auditability.
- **Distributed Transactions**: TC/C preferred for parallel execution over Saga for latency; phase status tables track progress.
- **Consensus**: Raft ensures event log replication; 3+ nodes per group for fault tolerance.
- **Technology Choices**:
  - RocksDB: LSM-tree for `<10,000` TPS per node, optimized writes.
  - mmap: Memory-maps event files for fast appends/caching.
  - CQRS: Separates read/write commands; read models poll/push events for real-time balance queries.

## Scalability & Bottlenecks
- **Horizontal Scaling**: Add Raft shards as load increases; TC/C coordinates cross-shard transfers.
- **Load Balancing**: Stateless coordinators scale elastically.
- **Caching**: Account balances cached in Raft groups; 80% hit rate reduces DB reads.
- **Replication**: Raft provides multi-AZ durability; auto-failover maintains high availability.
- **Bottlenecks**: Event log replay on failures; mitigated by periodic snapshots in HDFS. Cross-shard transactions limit scaling; shard by account ID minimizes inter-shard ops.

## Trade-offs & Alternatives
- **TC/C vs Saga**: TC/C offers lower latency via parallelism but risks momentary inconsistency; Saga is sequential, safer for validation-heavy ops.
- **SQL vs NoSQL**: SQL (e.g., PostgreSQL) chosen for ACID; NoSQL (e.g., Cassandra) could scale better but lacks transactional guarantees.
- **In-memory vs Persistent**: Pure Redis scales quickly but isn't durable; event sourcing adds auditability at cost of complexity.
- **Push vs Poll**: Push architecture enables real-time updates but increases resource usage; poll is simpler but not real-time.

## Future Improvements
- Add multi-currency support with FX rates.
- Integrate with external payment rails (banks, cards).
- Implement rate limiting and fraud detection (e.g., velocity checks).
- Use gRPC for internal communication to reduce latency.
- Add global transaction monitors for end-to-end observability.
- Explore deterministic databases (e.g., FoundationDB) for simpler distributed tx.

## Interview Talking Points
1. Event sourcing enables auditability by replaying immutable events, proving correctness through reproducible state.
2. TC/C provides efficient distributed transactions with compensation, avoiding locks compared to 2PC.
3. Raft consensus ensures high availability with >50% node survival, replicating events for durability.
4. Sharding by account ID minimizes cross-shard transactions, but requires compensation for distribution.
5. CQRS separates reads/writes, optimizing for write-heavy transfers and real-time balance queries.
6. Snapshots reduce replay time from hours to seconds, critical for quick recoveries.
7. High TPS achieved via local storage (RocksDB) and mmap caching, avoiding network bottlenecks.
