+++
title= "Online Banking System"
tags = [ "system-design", "software-architecture", "interview", "banking", "security", "finance" ]
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
weight= 36
bookFlatSection= true
+++

---

## Design Online Banking System

### Problem Statement
Design a secure and scalable online banking platform that handles customer accounts, transactions, and financial services. The system must ensure data privacy, regulatory compliance, fraud detection, and high availability while supporting millions of concurrent users making financial decisions that impact their lives and businesses.

### Requirements

#### Functional Requirements
- Account management (checking, savings, credit cards)
- Secure fund transfers and payments
- Transaction history and statements
- Bill payment and automatic payments
- Multi-factor authentication and security
- Fraud detection and alerts
- Mobile check deposit and ATM integration

#### Non-Functional Requirements
- PCI DSS and regulatory compliance (SOX, GDPR)
- Bank-level security with end-to-end encryption
- 99.999% availability for core banking functions
- Real-time transaction processing and balance updates
- Audit trails and comprehensive logging

### Key Constraints & Assumptions
- **Scale assumptions**: 50M active customers, 1M daily transactions, peak 10k/sec during business hours; 99.999% uptime required ^[Assumption: Equivalent to major bank's online scale.]
- **SLA**: 99.999% availability, p99 transaction latency `<2s`, financial accuracy to 1 cent always
- **Security**: Multi-layered security with fraud prevention losing money to convenience
- **Compliance**: Strict industry regulations (PCI DSS, SOX, GDPR) with immutable audit trails

### High-Level Design
The system implements a secure layered architecture with PCI-compliant payment processing zones. Customer-facing services are isolated from sensitive financial processing through API gateways and micro-segmentation.

```
graph TD
    A[Customer Web/Mobile] --> B[API Gateway]
    B --> C[Authentication Service]
    C --> D[MFA Provider]
    C --> E[Session Management]
    B --> F[Account Service]
    F --> G[PostgreSQL DB]
    B --> H[Transaction Service]
    H --> I[Secure Ledger DB]
    H --> J[Fraud Detection Engine]
    K[Payment Processor] --> L[PCI Zone]
    L --> M[ACH/EFT Networks]
    N[Compliance Auditing] --> O[Immutable Logs]
    P[Batch Processing] --> Q[Overnight Settlements]
    R[Third-party APIs] --> S[Bill Pay Vendors]
    T[ATM/POS Networks] --> U[Card Processing]
```

^[Mermaid diagram showing secure layered banking architecture with compliance boundaries.]

### Data Model
- **Customer Accounts**: PostgreSQL with strong consistency for account balances and customer data
- **Transactions**: Append-only ledger with immutable transaction history and double-entry accounting
- **Security Events**: Time-series database for login attempts, suspicious activities, and audit logs
- **Financial Products**: Relational modeling for loans, credit cards, investments with complex business rules

### API Design
Secure REST APIs with OAuth2 and MFA:

- **POST /api/v1/auth/login** - Authentication: `{"username": "user", "password": "hash", "device_fingerprint": "..."}` → MFA challenge or JWT token
- **GET /api/v1/accounts/{accountId}/balance** - Get balance: Returns current balance with last updated timestamp
- **POST /api/v1/transfers** - Fund transfer: `{"fromAccount": "123", "toAccount": "456", "amount": 100.00, "memo": "Rent"}` → `{"transactionId": "txn_789", "status": "pending"}`
- **GET /api/v1/transactions?account=123&dateRange=2023-01-01,2023-12-31** - Transaction history with pagination
- **POST /api/v1/payments/pay** - Bill payment: `{"payee": "Utility Co", "accountFrom": "123", "amount": 150.00, "dueDate": "2023-02-01"}`
- **WebSocket /alerts/{userId}** - Real-time fraud alerts and unusual activity notifications

^[APIs implement rate limiting, encryption (TLS 1.3), and comprehensive audit logging.]

### Detailed Design
- **Authentication**: Multi-factor using biometrics, device certificates, and behavioral analysis
- **Transaction Processing**: Synchronous processing with immediate balance validation and fraud checks
- **Fraud Detection**: Real-time ML models analyzing transaction patterns, location data, and device fingerprints
- **PCI Compliance**: Network segmentation with dedicated PCI zone for card processing and tokenization
- **Ledger System**: Double-entry bookkeeping with reconciliation jobs and balance verification
- **Backup & Recovery**: Multi-region replication with point-in-time recovery for financial data
- **Audit System**: Full audit trail of all user actions, system changes, and transaction processing
- **Regulatory Reporting**: Automated report generation for SOX, FDIC, and other regulatory bodies

### Scalability & Bottlenecks
- **Horizontal Scaling**: Stateless services auto-scale based on transaction volume per region
- **Database Sharding**: Account data sharded by customer ID, transactions by time ranges for scalability
- **Read Replicas**: Global read replicas for balance inquiries, strong consistency for writes
- **Load Balancing**: Regional load balancers route traffic to closest data centers minimizing latency
- **Bottlenecks**: End-of-month processing surges; mitigated by dedicated batch processing clusters

### Trade-offs & Alternatives
- **Security vs Usability**: Stricter security controls reduce convenience vs. user-friendly interfaces increase risk
- **Real-time vs Batch Processing**: Instant transaction visibility provides better UX vs. batch processing more secure/regulatory compliant
- **Centralized vs Branch Processing**: Central processing enables consistency vs. distributed increases complexity
- **Regulatory Compliance**: Stringent compliance requirements drive architecture decisions and service isolation

### Future Improvements
- Biometric authentication and behavioral biometrics
- Real-time financial insights and AI-powered advice
- Blockchain-based secure transaction processing
- Open banking APIs for third-party integration
- Advanced fraud detection with federated learning

### Interview Talking Points
1. Explain PCI compliance: Network segmentation separates card data from general IT systems for enhanced security
2. Discuss fraud prevention: ML models analyze real-time transaction patterns with behavioral scoring
3. Address availability: Multi-region replication with automatic failover maintains 99.999% uptime
4. Compare consistency: Strong consistency for financials vs. eventual consistency for non-critical data
5. Handle scale: Sharding by customer segments routes traffic geographically for better performance
6. Security-first design: Defense-in-depth with multi-layered controls balances security and usability
7. Regulatory challenges: Audit requirements drive immutable logging and traceability features
8. Transaction integrity: Double-entry accounting and reconciliation ensure financial accuracy
