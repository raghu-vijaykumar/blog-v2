+++
title= "Google Drive"
tags = [ "system-design", "software-architecture", "interview", "google-drive" ]
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
weight= 12
bookFlatSection= true
+++

# Design Google Drive

## Problem Statement
Design a cloud storage service similar to Google Drive that allows users to store, sync, and share files across multiple devices with scalability for millions of users and petabytes of data.

## Requirements

### Functional Requirements
- Upload, download, and delete files
- Sync files across multiple user devices in real-time
- Organize files in folders and share with other users
- Version control for files to allow rollback

### Non-Functional Requirements
- High availability (99.9% uptime)
- Low latency for file access (< 100ms for metadata operations)
- Strong consistency for file operations
- Scalability to handle millions of users and billions of files

## Key Constraints & Assumptions
- 100 million users, each with 1GB average storage (100 PB total data)
- Peak load: 10 million concurrent users
- File size up to 5GB, with average file size 10MB
- Read/write ratio: 100:1
- Latency SLA: < 1s for file uploads, < 100ms for listing files
- Assumption: Users have multiple devices (mobile, desktop) requiring sync

## High-Level Design

The system uses a distributed architecture with client applications, load balancers, metadata service, storage nodes, and synchronization services. Clients upload/download files via HTTPS, metadata is stored in a sharded SQL/NoSQL database, files are distributed across storage nodes using consistent hashing, and sync is handled via long polling.

```mermaid
graph TD
    A[Client] --> B[Load Balancer]
    B --> C[API Gateway]
    C --> D[Metadata Service]
    C --> E[File Sync Service]
    D --> F[Database (Sharded)]
    E --> G[Storage Nodes (Consistent Hashing)]
    G --> H[File System]
```

### Components
- **Client**: Web/mobile/desktop apps for file operations and sync
- **Load Balancer**: Distributes requests across API servers
- **API Gateway**: Authentication, routing, rate limiting
- **Metadata Service**: Manages file metadata (CRUD operations)
- **File Sync Service**: Handles cross-device synchronization
- **Storage Nodes**: Distributed file storage with load balancing
- **Database**: Sharded metadata storage (SQL for transactions, possible NoSQL hybrid)

## Data Model

### Entities
- **User**: user_id, email, storage_quota
- **File**: file_id, user_id, name, path, size, checksum, versions[], permissions, timestamps
- **Folder**: folder_id, user_id, name, parent_id, path
- **Version**: version_id, file_id, checksum, delta_data (for incremental updates)

### Storage Choice
- Metadata: Sharded relational database (e.g., MySQL with Vitess) for transactional consistency; user_id as shard key
- Files: Object storage (e.g., blob storage like S3) with consistent hashing distribution
- Assumption: Metadata table ~100 billion rows, indexed on user_id for efficient queries

## API Design

Core endpoints for file operations:
- `POST /upload` - Upload file (multipart with metadata)
- `GET /files/{file_id}/download` - Download file
- `PUT /files/{file_id}` - Update file metadata
- `DELETE /files/{file_id}` - Delete file
- `GET /files?user_id=X&path=Y` - List files in folder
- `POST /files/{file_id}/share` - Share with users/groups

Sample request:
```
POST /upload
Headers: Authorization: Bearer <token>, Content-Type: multipart/form-data
Body: file=<binary>, metadata={"name": "doc.pdf", "path": "/docs/"}
```

Sample response:
```
{
  "file_id": "abc123",
  "status": "uploaded",
  "checksum": "md5hash"
}
```

## Detailed Design

### File Upload Flow
1. Client compresses file (optional) and splits into blocks with CRC
2. Uploads via HTTPS to load balancer
3. API Gateway authenticates and routes to metadata service
4. Metadata service saves metadata in DB, gets storage node via consistent hashing
5. Uploads blocks to storage node; only modified blocks for deltas
6. Updates sync service for cross-device notification

### Metadata Management
- Sharded by user_id to ensure user data locality
- Indexes on path for efficient folder listings
- Replication for fault tolerance (master-slave)

### Synchronization
- Uses long polling: clients poll sync service every 5-30s for changes
- Push notifications for critical updates; WebSockets for bi-directional if needed
- Change detection via checksums (CRC) and timestamps

### Caching
- CDN for popular static files
- Redis for metadata caching (user's recent files)

## Scalability & Bottlenecks

### Horizontal Scaling
- Auto-scaling storage nodes; consistent hashing minimizes data movement when adding nodes
- Database sharding with rebalancing for even load
- Load balancing with session affinity for sync connections

### Bottlenecks & Solutions
- Large file uploads: Delta copying + compression (reduces bandwidth by ~80%)
- High read load: Caching layers (CDN + Redis) handle 90% of reads
- Metadata queries: Sharding ensures per-user scaling
- Network latency: Edge servers with CDN reduce access time
- Assumption: 10 PB data requires 1000 storage nodes (100TB each); auto-rebalancing prevents hotspots

## Trade-offs & Alternatives

- SQL vs NoSQL: SQL chosen for strong consistency and complex queries; NoSQL alternative for cheaper scale but eventual consistency trade-off
- Consistent Hashing vs Rendezvous: Consistent hashing selected for even load distribution; simpler alternatives fail under churn
- Long Polling vs WebSockets: Polling chosen for battery efficiency on mobile; WebSockets offer lower latency but higher overhead
- Compression: Brotli vs Gzip - Brotli better compression (~20% more) but CPU intensive; trade-off with battery usage
- Delta Sync: Incremental sync reduces bandwidth but increases metadata storage (CRC per block)

## Future Improvements

- Peer-to-peer sync for nearby devices to reduce server load
- AI-driven deduplication for shared files
- Global replication with active-active architecture for faster worldwide access
- End-to-end encryption for privacy
- Real-time collaboration features (like Google Docs integration)

## Interview Talking Points

1. How consistent hashing ensures minimal data movement when scaling storage nodes
2. Why SQL for metadata despite NoSQL's scale - transactional consistency for file operations
3. Delta copying benefits and trade-offs compared to full file uploads
4. Sync protocol choice: long polling vs WebSockets balance of efficiency and battery life
5. Sharding strategy by user_id and why it works for isolation and load
6. Caching layers and how they handle read-heavy workloads
7. Fault tolerance - how replication and auto-rebalancing prevent single points of failure
