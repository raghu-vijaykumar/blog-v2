+++
title= "Collaborative Document Editing"
tags = [ "system-design", "software-architecture", "interview", "collab", "real-time", "operational-transform" ]
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
weight= 34
bookFlatSection= true
+++

---

## Design Collaborative Document Editing

### Problem Statement
Design a real-time collaborative document editing platform where multiple users can simultaneously edit the same document. The system must handle operational conflicts, maintain consistency across all users, and provide low-latency synchronization while supporting rich text formatting, comments, and version history.

### Requirements

#### Functional Requirements
- Real-time document editing with multiple concurrent users
- Rich text formatting (bold, italic, fonts, colors)
- Commenting system for document sections
- Version history and conflict resolution
- Document sharing with access control (view/edit permissions)
- Offline editing with automatic sync

#### Non-Functional Requirements
- Sub-second latency for character-level changes
- Strong eventual consistency across all user views
- High availability with automatic failover
- Support for large documents (100k+ characters)
- Scalability to millions of active documents

### Key Constraints & Assumptions
- **Scale assumptions**: 100M documents, 10M daily active editors, up to 50 concurrent editors per document; 1M new changes/minute ^[Assumption: Based on collaborative tools usage patterns.]
- **SLA**: 99.9% availability, p99 latency `<200ms` for edits, `<500ms` for sync
- **Concurrent editing**: Average 5-10 users per document, peak 50 users for large documents
- **Document size**: Most documents `<50MB`, with media attachments up to 1GB

### High-Level Design  
The system uses operational transformation (OT) or CRDTs for conflict-free replicated data types to handle concurrent edits. WebSocket connections enable real-time sync, with a central server managing document state and broadcasting changes.

```
graph TD
    A[User A Browser] --> B[WebSocket Gateway]
    C[User B Browser] --> B
    B --> D[Doc Sync Service]
    D --> E[Redis Cache]
    E --> F[Document State]
    D --> G[PostgreSQL DB]
    D --> H[Kafka Event Stream]
    H --> I[Version History Service]
    H --> J[Notification Service]
    K[Auth Service] --> L[Access Control]
    L --> D
    M[File Storage] --> N[Media Attachments]
    G --> O[Document Metadata]
    I --> P[S3 for Snapshots]
```

^[Mermaid diagram showing OT-based collaborative editing with real-time synchronization.]

### Data Model
- **Documents**: JSON format in PostgreSQL for metadata; document content stored as revision snapshots in object storage
- **Operations**: Incremental operational changes stored in time-series database for replay capabilities  
- **Versions**: Point-in-time snapshots stored in S3 with diff compression for storage efficiency
- **Users & Permissions**: Relational storage for user roles and document-level access control
- **Comments**: Threaded discussion system with position anchors in the document

### API Design
WebSocket-based real-time sync with REST APIs:

- **POST /api/v1/documents** - Create document: `{"title": "Team Report", "content": "initial content"}` → `{"documentId": "doc123", "sessionToken": "abc..."}}`
- **WebSocket /sync/{documentId}** - Real-time sync: Send operations like `{"type": "insert", "position": 10, "text": "hello"}`
- **GET /api/v1/documents/{documentId}/versions** - Get version history with diffs
- **POST /api/v1/documents/{documentId}/share** - Share with users: `{"email": "user@domain.com", "permission": "edit"}` → share token created
- **GET /api/v1/documents/{documentId}/comments** - Fetch comments with position mapping
- **WebSocket receipts**: Receive transformed operations from server for all connected clients

^[APIs support operational transformation acknowledgments and error recovery.]

### Detailed Design
- **Operational Transformation**: Server receives operations, transforms them against concurrent changes, broadcasts to all clients
- **CRDT Alternative**: Text is modeled as Conflict-Free Replicated Data Type where insertions/deletions commute
- **Sync Engine**: Go-based service handling operation queues and state synchronization
- **Cache Layer**: Redis stores recent document states in memory for fast access
- **Conflict Resolution**: Server-side transformation ensures converged state across all clients
- **Version Control**: Git-like branching with reconciliation for complex merges
- **Persistence**: Incremental snapshots every 5 minutes, full version history retention
- **WebSocket Clustering**: Sticky sessions ensure all edits in a document route to same server instance

### Scalability & Bottlenecks
- **Horizontal Scaling**: Stateless sync services scale by document ID sharding
- **Caching Strategy**: Document state cached with write-through to persistent storage
- **Operation Batching**: Group multiple micro-operations into single broadcasts to reduce bandwidth
- **Document Partitioning**: Large documents split by chapters/sections for parallel editing
- **Bottlenecks**: Memory usage for large collaborative documents; mitigated with pagination and section-based lock

### Trade-offs & Alternatives
- **OT vs CRDTs**: OT easier to implement for rich text vs. CRDTs more scalable and robust for conflicts
- **Centralized vs Decentralized**: Central server simplifies consistency vs. decentralized (no single point of failure)
- **Eager vs Lazy Sync**: Real-time sync ensures consistency vs. lazy sync more tolerant of network issues
- **Full History vs Snapshots**: Comprehensive history enables undo vs. snapshots reduce storage complexity

### Future Improvements
- Advanced collaboration features (cursors, selection tracking)
- AI-powered merge conflict resolution
- Integration with external file formats (MS Word, PDF)
- Mobile-optimized editing interfaces
- Anonymous editing with change attribution

### Interview Talking Points
1. Explain OT: Operational transformation ensures all clients converge to same state through concurrent change resolution
2. Discuss collaboration scale: Sharding by document ID enables millions of active editing sessions
3. Address conflict resolution: Server transforms operations to prevent data loss during concurrent edits
4. Compare OT vs CRDTs: OT simpler for text editing vs. CRDTs mathematically proven consistency
5. Handle network interruptions: Buffer operations client-side for offline editing and sync on reconnection
6. Optimize bandwidth: Compress operations and batch broadcasts to reduce real-time traffic
7. Persistence strategy: Incremental snapshots balance performance vs. storage efficiency
8. Edge cases: Handle rapid successive edits from multiple users without state corruption
