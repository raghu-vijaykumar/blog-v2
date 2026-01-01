+++
title= "Email Service"
tags = [ "system-design", "software-architecture", "interview", "email-service" ]
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
weight= 22
bookFlatSection= true
+++

## Design Distributed Email Service

### Problem Statement
Design a distributed email service similar to Gmail that supports sending and receiving emails at scale for billions of users. The system must handle email composition, delivery, retrieval, storage, and search across web clients, while ensuring high reliability and availability for user data.

### Requirements

#### Functional Requirements
- Send and receive emails with attachments up to 25MB
- Fetch and display emails from folders (Inbox, Sent, etc.)
- Filter and search emails by subject, sender, content, and read/unread status
- Support conversation threads via email headers
- Anti-spam protection and virus scanning

#### Non-Functional Requirements
- Reliability: Prevent data loss with strong consistency guarantees
- Availability: Tolerate partial failures and use replication for high uptime (99.9%+ SLA)
- Scalability: Handle 1 billion users sending/receiving up to 100,000 emails/second
- Flexibility and Extensibility: Use HTTP APIs for easy feature additions

### Key Constraints & Assumptions
- **Users**: 1 billion active users (based on Gmail/Outlook scale)
- **Traffic**: 10 emails sent per user/day (100,000 sends/second peak) *Assumption: moderate usage*
- **Storage**: 730 PB/year metadata (50KB avg/message) + 1,460 PB/year attachments (500KB avg, 20% have attachments)
- **Latency**: p999 < 500ms for read operations, < 2s for delivery *Assumption: sub-second UX expected*
- **Email Connections**: HTTP APIs for web clients (SMTP/IMAP for legacy compatibility if needed)
- **Attachments**: Base64 encoded, max 25MB size limit
- No authentication deep dive *Assumption: handled externally*

### High-Level Design

The system uses a distributed architecture to support web-based email clients connecting via HTTP APIs, with SMTP for inter-server communication. Key components include load balancers for traffic distribution, web servers for request handling, metadata databases for email storage, object stores for attachments, caching for performance, and search engines for querying. Real-time servers push updates using WebSockets or long-polling. Message queues handle async processing of sent/received emails with spam/virus checks.

Component roles:
- **Webmail Client**: User interface for composing and viewing emails
- **Load Balancer**: Routes requests, enforces rate limits (e.g., max sends/user/minute)
- **Web Servers**: Handle API requests, basic validation, spam checks
- **Metadata DB**: Stores email headers, bodies, user data (NoSQL like Cassandra for scalability)
- **Attachment Store**: Distributed object storage (e.g., S3) for large files
- **Cache**: In-memory store (e.g., Redis) for recent emails and hot data
- **Search Store**: Full-text search engine (e.g., Elasticsearch) with user-partitioned indexes
- **Message Queue**: Async processing for outgoing/incoming emails (e.g., Kafka)
- **SMTP Servers**: Handle sending/receiving emails between domains
- **Real-time Servers**: Push notifications for new emails (WebSockets + long-polling fallback)

```
graph TB
    A[Webmail Client] --> B[Load Balancer]
    B --> C[Web Servers]
    C --> D[Message Queue Outgoing/Error]
    C --> E[Metadata DB]
    D --> F[SMTP Servers]
    F --> G[External Mail Servers]
    H[External Mail Servers] --> I[SMTP Servers Incoming]
    I --> J[Message Queue Incoming]
    J --> K[Mail Processing Workers]
    K --> E
    K --> L[Attachment Store]
    K --> M[Cache]
    K --> N[Real-time Servers]
    C --> M
    C --> N
    E --> O[Search Store]
    M --> E
```

### Data Model
- **Storage Choice**: Distributed NoSQL database (e.g., Cassandra) for high write throughput, horizontal scaling, and fault tolerance. User_id as partition key for sharding (each user's data on one shard). Strong consistency prioritized over availability for email reliability.
- **Entities**:
  - **Users**: Basic profile data (email, name)
  - **Folders**: Per-user collections (Inbox, Sent, etc.) with default folders per RFC6154
  - **Emails**: Header/body data, threading headers (Message-Id, In-Reply-To), read status. TimeUUID for sorting by creation time.
  - **Attachments**: Separate table/store; referenced by filename; deduplicated to avoid redundancy.
- **Schema Sketch** (Cassandra-style):
  - Folders: (user_id PK, folder_id CK, name, ...)
  - Emails: (user_id PK, folder_id, email_id timeuuid CK, from, to, subject, body, is_read, threading headers)
  - ReadEmails/UnreadEmails: Denormalized tables to avoid filtering on non-key columns
  - Attachments: (filename PK, user_id, email_id, content_ref, size)

### API Design (if relevant)
RESTful endpoints using JSON for web clients:

- `POST /v1/messages` - Send email to recipients in To/Cc/Bcc
  - Request: `{ "to": [{"name": "string", "email": "string"}], "subject": "string", "body": "string", "attachments": ["filename"] }`
- `GET /v1/folders` - List user folders
  - Response: `[{ "id": "string", "name": "string", "user_id": "string" }]` (defaults: All, Archive, Drafts, Flagged, Junk, Sent, Trash)
- `GET /v1/folders/:id/messages?offset=0&limit=50` - Paginated messages in folder with sorting by time
  - Response: `[{ "id": "string", "from": {"name": "string", "email": "string"}, "to": [...], "subject": "string", "body": "string", "is_read": bool }]`
- `GET /v1/messages/:id` - Full email details
- `PUT /v1/messages/:id/read` - Mark as read/unread
- `GET /v1/search?q=term&filters=from:alice,subject:hello` - Full-text search with filters

*Assumption: Attach webh/w to Attachment Store for uploads*

### Detailed Design

#### Metadata Database
Characteristics: Small frequent headers; variable body sizes; user-isolated operations; recency bias (recent emails accessed most); zero data loss required. Custom DB needed for Gmail-scale IOPS optimization (reduce disk seeks).

- Partitioning: `user_id` as shard key (no cross-user sharing). Clustering key (timeuuid) for temporal sorting.
- Denormalization: Separate read/unread email tables to support efficient filtering (Cassandra limitation on non-key columns).
- Threading: Email headers (Message-Id, References) for client-side conversation reconstruction.
- Backup: Incremental snapshots for fault tolerance.

#### Attachment Store
Distributed object storage (e.g., S3-compatible) for scalable binary storage. Attachments referenced by filename in DB; deduplicated by hash *Assumption: content-based hashing* to save space. Base64 handling on upload/download.

#### Cache Layer (Redis)
Cache recent/hot emails and user sessions. Reduces DB load for frequent reads. TTL-based eviction for storage efficiency.

#### Search Store
Elasticsearch for full-text indexing. `user_id` partitioned for user-isolation. Async reindexing via Kafka on mutations (writes >> reads). Alternative: Custom LSM-tree based engine for write-optimization (like BigTable). Trade-off: Off-the-shelf elasticity vs. custom fine-tuning.

#### Message Processing
- **Outgoing**: Rate limiting -> validation (size, format) -> spam/virus scan -> queue -> SMTP routing with retries (exponential backoff). Monitor queue size for scaling.
- **Incoming**: SMTP acceptance -> size checks -> queue -> processing (storage + real-time push) -> attachment offload if large.
- Queues: Kafka for durability; separate error queues for dead letters.

#### Real-time Servers
WebSocket primary, long-polling fallback. Push new email notifications to connected clients.

#### SMTP Servers
Standard MTAs for inter-domain delivery. MX record DNS lookups. Email authentication (SPF, DKIM) for deliverability; IP warming to avoid spam filters.

### Scalability & Bottlenecks
- **Horizontal Scaling**: Independent components (webservers, DB shards, Elasticsearch nodes). Auto-scale based on CPU/memory metrics. DB sharding by `user_id`; cache sharding by consistent hashing.
- **Load Balancing**: L7 LB for API routing; L4 for SMTP. Rate limiting to prevent abuse (e.g., 1000 sends/hour/user).
- **Replication**: DB leader-follower; multi-AZ/multi-DC for geo-distribution and failover. Cache replication across regions for low latency.
- **Bottlenecks & Optimizations**: Disk I/O (LSM-trees); attachment deduplication; queue monitoring for consumer scaling. Compression for large bodies. CDNs for attachment delivery *Assumption: geographically distributed*.
- **Peak Handling**: 100k emails/sec sustained; burst tolerance with buffers.

### Trade-offs & Alternatives
- **DB Choice**: NoSQL (consistency over availability) vs. Relational (rich queries but less scalable). Alternative: Custom DB (control vs. maintenance cost).
- **Search**: Elasticsearch (mature, full-text) vs. Custom engine (optimized writes vs. dev effort). LSM-trees for I/O efficiency but complex implementation.
- **Protocols**: HTTP (flexible web) vs. Native SMTP/IMAP (legacy support vs. complexity).
- **Caching**: Redis (fast, in-memory) vs. Memcached (simpler but less features).
- **Availability vs. Consistency**: Prioritize consistency (email reliability) over 100% uptime during failures.
- **Monolith vs. Microservices**: Services decoupled via queues for independent scaling vs. higher operational overhead.

### Future Improvements
- Advanced filtering/search (AI-powered spam, semantic search)
- GDPR-compliant data retention/deletion
- End-to-end encryption for security
- Attachment optimization (compression, thumbnail generation)
- Cross-platform sync (mobile/PWA integration)
- Analytics dashboard for user metrics

### Interview Talking Points
1. User sharding choice: Isolation boosts security and scaling but complicates cross-user features.
2. NoSQL trade-off: Scales writes horizontally but requires denormalization for queries.
3. HTTP over SMTP: Simplifies APIs for web but adds translation layer complexity.
4. Email deliverability: Reputation management critical; IP warming prevents spam flagging.
5. Consistency priority: Emails demand reliability over top availability.
6. Search optimization: Writes dominate, so async indexing with LSM-trees.
7. Real-time pushes: WebSockets reduce polling but need fallback for compatibility.
8. Queue monitoring: Essential for elasticity and failure detection.
