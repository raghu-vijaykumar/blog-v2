---
title: "Payment System"
tags: [ "system-design", "software-architecture", "interview", "payment-system" ]
author: "Me"
showToc: true
TocOpen: false
draft: false
hidemeta: false
comments: false
disableShare: false
disableHLJS: false
hideSummary: false
searchHidden: true
ShowReadingTime: true
ShowBreadCrumbs: true
ShowPostNavLinks: true
ShowWordCount: true
ShowRssButtonInSectionTermList: true
UseHugoToc: true
weight: 25
bookFlatSection: true
---

# Design Payment System

## Problem Statement
A scalable, reliable, and secure e-commerce payment system that processes credit card transactions, handles fraud detection, and ensures high availability for merchants. The system prioritizes transaction consistency, PCI DSS compliance, and fault tolerance to facilitate secure monetary exchanges in online purchasing.

## Requirements

### Functional Requirements
- Authorize and capture card payments via RESTful APIs
- Process refunds and chargebacks
- Integrate with third-party PSPs (Payment Service Providers) for card validation and routing
- Handle fraud detection and risk assessment
- Provide transaction history and reconciliation
- Support multiple currencies and international transactions
- Generate and manage payment events for asynchronous processing

### Non-Functional Requirements
- 99.99% availability (downtime `<4` hours/year)
- Process 10,000 TPS (transactions per second) during peak *Assumption: Based on large e-commerce scale*
- End-to-end latency `<5` seconds for authorizations
- PCI DSS compliance for data security
- Idempotency for retry-safe operations
- Zero financial loss tolerance (ACID-like properties)

## Key Constraints & Assumptions
- **Scale**: 10,000 TPS, 10M daily transactions, 1PB annual data *Assumption: 100 yearly active users, 10% transaction rate*
- **Compliance**: PCI DSS for card data, GDPR for user data; Assume PSD2 in Europe for SCA (Strong Customer Authentication)
- **Latency SLA**: `<5s` for auth, `<1s` for validations; `<2 min` for refunds
- **Data**: 70% credit cards, 20% digital wallets, 10% bank transfers *Assumption*
- **Costs**: Prioritize reliability over cost; encryption and TLS mandatory
- **Faults**: Expect 1-2 failures/hour in dependencies; network partitions possible

## High-Level Design
The system orchestrates payments through PSPs, banks, and internal services, using async messaging for fault tolerance. Key components include a payment gateway, event-driven service bus, fraud engine, and ledger for reconciliation.

Components:
- **Payment Gateway**: Entry point; validates requests, routes to PSPs
- **PSP Integrator**: Interfaces with third-party PSPs for card processing
- **Fraud Service**: Real-time risk checks using ML models
- **Ledger Service**: Records all financial transactions for auditing
- **Event Bus (Kafka)**: Handles async flows, retries, and event sourcing
- **Wallet Service**: Manages merchant/buyer balances

```
%%{init: {'theme': 'neutral'}}%%
graph TD
    Client[Client] --> Gateway[Payment Gateway]
    Gateway --> Fraud[Fraud Service]
    Fraud --> PSP[PSP Integrator]
    PSP --> Banks[Acquiring/Issuing Banks]
    PSP --> EventBus[Kafka Event Bus]
    EventBus --> Ledger[Ledger Service]
    EventBus --> Wallet[Wallet Service]
    EventBus --> Notification[Notification Service]

    classDef component fill:#e1f5fe,stroke:#01579b
    class Gateway,Fraud,PSP,EventBus component
    classDef external fill:#fff3e0,stroke:#ff9800
    class Banks external
```

## Data Model
### Payments Table
- `payment_id` (UUID, Primary Key)
- `user_id` (UUID, FK)
- `merchant_id` (UUID, FK)
- `amount` (Decimal)
- `currency` (String, e.g., USD)
- `status` (Enum: pending, approved, declined, refunded)
- `created_at` (Timestamp)
- `idempotency_key` (String)
- `card_token` (Encrypted string, PCI compliant)

### Transactions Table
- `transaction_id` (UUID, PK)
- `payment_id` (UUID, FK)
- `type` (Enum: auth, capture, refund)
- `gateway_response` (JSON)
- `processed_at` (Timestamp)

*Storage*: PostgreSQL for relational data (ACID for financials), MongoDB or Cassandra for audit logs; Encrypt sensitive data at rest.

## API Design
Core RESTful endpoints exposed via the Gateway:

- **Authorize Payment**: `POST /payments/authorize`  
  Body: `{ user_id, merchant_id, amount, currency, card_details, idempotency_key }`  
  Response: `{ payment_id, status: "approved", auth_code }`

- **Capture Payment**: `POST /payments/{payment_id}/capture`  
  Headers: Idempotency-Key  
  Response: `{ status: "captured", transaction_id }`

- **Refund Payment**: `POST /payments/{payment_id}/refund`  
  Body: `{ amount, reason }`  
  Response: `{ refund_id, status: "processed" }`

- **Get Payment Status**: `GET /payments/{payment_id}`  
  Response: JSON with payment details

*Assumptions*: OAuth2 for merchant auth; TLS 1.3 for encryption; Webhooks for async updates; Error codes: 400 Bad Request, 402 Payment Required (decline), 409 Conflict (idempotency fault)*

## Detailed Design
### Payment Gateway
- Stateless API layer (Node.js/Go) for request routing and rate limiting *Reason: Scale with Kubernetes, rapid deployment*
- Integrates with PSPs via SDKs; handles tokenization for PCI compliance

### Event-Driven Architecture
- Uses Kafka for event persistence: PaymentCreated, PaymentAuthorized, etc.
- Consumers handle workflows: Fraud checks, ledger updates, notifications
- Dead-letter queues for failed messages; Circuit breakers to isolate failures

### Fraud Service
- ML-based risk scoring (e.g., anomaly detection on velocity, geo)
- Integrates with external providers (e.g., Stripe Radar) for group insights
- **Choice**: Async processing to avoid auth delays *Trade-off: Latency vs accuracy; but essential for security*

### Ledger Service
- Immutable event-sourced journal in RDBMS; Cached views for balance queries
- Handles chargebacks and disputes with audit trails
- **Choice**: PostgreSQL for serializable transactions *Reason: ACID for financial accuracy over NoSQL speed*

### Retry & Fault Handling
- Exponential backoff with jitter for PSP calls
- Idempotency via keys prevents double-charging
- Fallback: Offline mode for small amounts if services down

## Scalability & Bottlenecks
- **Horizontal Scaling**: Gateway and services scale via load balancers; Kafka partitions for throughput
- **Sharding**: Payments sharded by merchant_id/hash(user_id); Geo-sharding for regions
- **Replication**: Read replicas for reporting; Multi-AZ for resilience
- **Caching**: Redis for session states, fraud rules; TTL for short-lived data
- **Load Balancing**: DNS-based geo-routing; Circuit breakers prevent cascade failures
- **Bottlenecks**: PSP limits (e.g., 100 TPS per merchant); Mitigate with queues
- **IOPS**: Scale DB nodes; Use SSDs for high-write logs
- **Peak Handling**: Auto-scale pods; Buffer spikes in Kafka

## Trade-offs & Alternatives
- **Sync vs Async Processing**: Sync for real-time auth but risks cascading fails; Async via Kafka improves resilience but adds complexity. Chose hybrid: Sync for core auth, async for post-processing.
- **Monolith vs Microservices**: Monolith simpler initially but hard to scale; Microservices enable independent deploys but added inter-service comms. Chose microservices for team scalability.
- **In-House vs PSP**: Building in-house reduces fees but massive compliance burden; PSP delegates security but vendor lock-in. Used PSP integration for speed-to-market.
- **Relational vs NoSQL**: SQL for strict consistency in payments; NoSQL for audit logs speed. Chose blend for best of both.
- **Strong Consistency vs Eventual**: Strong ensures no double-spends; Eventual faster but requires compensation logic. Strong for financials to prevent disputes.

## Future Improvements
- Real-time fraud ML training with user data
- Support for cryptocurrencies and crypto wallets
- Blockchain for immutable audit trails
- Global expansion with localized compliance (e.g., SCA in EU)
- Analytics dashboard for merchant insights
- AI-driven personalized payment recommendations

## Interview Talking Points
1. Payment systems require ACID-like properties for financial data despite distributed nature.
2. Async event-driven flow via Kafka ensures fault-tolerance and resilience at scale.
3. Idempotency keys prevent duplicates during retries, critical for network faults.
4. PCI compliance mandates data minimization and encryption; tokenization offloads liabilities.
5. Fraud detection trade-off: Accuracy vs latency; ML models balance real-time decisions.
6. Circuit breakers and retries handle downstream failures without cascading outages.
7. Event sourcing in ledger provides auditability and simplifies reconciliation.
8. Assumptions: Target 10K TPS with distributed load; prioritize security over cost.
9. Scaling: Geo-sharding for global reach, auto-scaling for peak loads.
10. Alternatives: In-house gateway vs PSP saves fees but risks compliance failures.
