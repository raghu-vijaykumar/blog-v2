+++
title= "Video Sharing Platform"
tags = [ "system-design", "software-architecture", "interview", "video-sharing" ]
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
weight= 11
bookFlatSection= true
+++

# Video Sharing Platform

## Problem Statement
Design a scalable video on demand (VoD) streaming platform similar to YouTube or Netflix that allows content creators to upload videos and millions of users to search and stream content. The system must handle large video files, support adaptive bitrate streaming for optimal playback across devices and network conditions, ensure high availability and low latency, and support millions of users with sub-second search response times and zero buffering.

## Requirements

### Functional Requirements
- **Content Creator Features**: Upload videos in any format/codec, metadata management (title, author, description, tags), email notifications upon processing completion.
- **User Features**: Search videos by text queries, stream videos on any device/browser, adaptive bitrate for varying network conditions.
- **Video Management**: Asynchronous transcoding, multi-format support, DRM for content protection.

### Non-Functional Requirements
- **Scale**: 100s of millions of users, thousands of content creators, 1B+ videos stored.
- **Availability**: 99.99% uptime for users, 99.9% for creators.
- **Performance**: `<500ms` search latency, `<1s` streaming start, zero buffering goal, 500ms creator UI load.
- **Consistency**: Strong for creator data, eventual for user searches.
- **Durability**: 100% message persistence, geo-redundant storage.

## Key Constraints & Assumptions
- Text-based search only, no live streaming.
- Average video size: 50GB raw, up to 10GB compressed.
- Peak concurrent users: 50M.
- Global user base with multi-region deployment.
- End-to-end encryption and DRM required.
- Video processing completes within hours of upload.

## High-Level Design

The system uses a microservices architecture with event-driven processing for scalability. Key components include API Gateway for auth, Object Store for video assets, Transcoding/Packaging services for video processing, Search service for queries, and CDN for delivery.

```mermaid
graph TD
    Client[Client App/Web] --> API_Gateway[API Gateway]
    API_Gateway --> User_Service[User Service]
    API_Gateway --> Upload_Service[Upload Service]
    Upload_Service --> Object_Store[Object Store]
    Object_Store --> Message_Broker[Message Broker (Kafka)]
    Message_Broker --> Transcoding_Service[Transcoding Service]
    Transcoding_Service --> Packaging_Service[Packaging Service]
    Packaging_Service --> Object_Store
    Packaging_Service --> Video_Data_Service[Video Data Service]
    Video_Data_Service --> Search_Service[Search Service]
    Search_Service --> Cache[Redis Cache]
    CDN[CDN] --> Object_Store
    Client --> CDN
    API_Gateway --> Video_Data_Service
    Video_Data_Service --> DB[Metadata DB (SQL)]
    Search_Service --> Search_DB[Search DB (NoSQL)]
```

## Data Model

- **Users Table** (SQL): user_id (PK), email, username, password_hash, profile_url, role (creator/user), created_at.
- **Videos Table** (SQL): video_id (PK), creator_id (FK), title, description, tags (JSON), duration, status (uploaded/processing/ready), created_at.
- **Video_Files Table** (SQL): video_id (FK), format, bitrate, resolution, file_url, cdn_url.
- **Search_Index** (NoSQL): video_id, title, description, tags, creator, indexed_at.
- **Views Table** (NoSQL): video_id, user_id, timestamp, view_duration.

Storage: SQL (PostgreSQL) for structured metadata, NoSQL (Elasticsearch/DynamoDB) for search/indexing, Object Store (S3) for video files.

## API Design

- `POST /users/register`: Body `{email, password, username}`. Response  `{user_id, token}`.
- `POST /videos/upload`: Auth token, Body `{title, description, tags, file_url (pre-signed)}`. Response `{video_id}`.
- `GET /videos/search?q=query&limit=10`: Response `[{video_id, title, creator, thumbnail}]`.
- `GET /videos/{id}/stream`: Response `{manifest_url, drm_keys}`.
- `POST /videos/{id}/view`: Track user views.
Sample Upload API:
```
POST /videos/upload
Authorization: Bearer <token>
{
  "title": "Sample Video",
  "description": "A demo video",
  "tags": ["demo", "tutorial"],
  "file_url": "https://s3-presigned-url..."
}
```

## Detailed Design

- **API Gateway**: Auth via JWT, rate limiting, SSL termination, routes to microservices.
- **Upload Service**: Generates pre-signed S3 URLs for direct uploads, validates metadata.
- **Transcoding Service**: Processes videos using FFmpeg/ffmpeg, outputs multiple bitrates/resolutions.
- **Packaging Service**: Applies DRM (Widevine/PlayReady), creates HLS/DASH manifests.
- **Video Data Service**: Manages metadata, updates video status, triggers notifications.
- **Search Service**: Indexes videos in Elasticsearch, supports fuzzy search and filters.
- **Object Store & CDN**: S3 for storage, CloudFront for global delivery with edge caching.
- **Message Broker**: Kafka for decoupling processing steps, ensuring reliability.

Protocol: HLS/DASH for adaptive streaming, WebSockets for real-time updates (optional).

## Scalability & Bottlenecks

- **Horizontal Scaling**: All services containerized on Kubernetes with HPA. Shard databases by video_id/user_id.
- **Load Balancing**: Nginx/ALB for API traffic, consistent hashing for DB/queue routing.
- **Caching**: Redis for search results, user sessions, CDN for video segments.
- **Replication**: Cross-region DB replication, multi-AZ for availability.
- **Bandwidth**: Assume 1Gbps CDN capacity per region, scale with user growth.
*Bottlenecks: Transcoding CPU-intensive - use GPU acceleration; Storage I/O - SSDs and sharding.

## Trade-offs & Alternatives

- **Streaming Protocols**: HLS/DASH vs RTMP - HLS preferred for web compatibility; trade-off is higher latency (+2-3s).
- **Storage**: S3 vs self-managed - S3 chosen for durability/scalability; trade-off is vendor lock-in.
- **Search**: Elasticsearch vs custom indexing - ES for complex queries; trade-off is ops overhead.
- **DRM**: Client-side vs server-side - Client-side (Widevine) standard for OTT; trade-off is complexity vs security.
- **Monolithic vs Microservices**: Microservices for scalability; trade-off is operational complexity.

## Future Improvements

- Support for live streaming and interactive features.
- AI-powered content recommendations and personalization.
- Advanced analytics and monetization (ads, subscriptions).
- Global expansion with multi-region active-active architecture.
- Integration with social features (comments, sharing).

## Interview Talking Points
1. Why microservices? Enables independent scaling and fault isolation.
2. Scalability challenge: Handling 50GB videos and PB-scale storage.
3. Streaming protocols: HLS/DASH for adaptive bitrate to eliminate buffering.
4. DRM importance: Protects premium content from piracy.
5. CDN role: Reduces latency for global users through edge caching.
6. Database sharding: By video_id to distribute read/write load.
7. Consistency trade-off: Eventual consistency for search availability.
8. Transcoding bottlenecks: Async processing with message queues.
9. Caching strategies: Multi-level for metadata, search, and video segments.
10. Deployment: Kubernetes for orchestration, auto-scaling based on load.
