title: IDs & Time

# IDs & Time

Build correct, scalable primitives for identity and ordering. This module covers how to choose and generate IDs (random vs sortable vs sequenced), how time works in distributed systems (wall vs monotonic, skew and synchronization), and how to make ordering, TTLs, retries, and idempotency robust in production.

What you will learn
- ID properties and schemes: UUID v1/v4/v7, ULID/KSUID/XID, Snowflake-like, database sequences, natural keys
- Sortability, partition locality, and hotspot mitigation for write paths and range scans
- Time fundamentals: wall vs monotonic clocks, NTP/PTP, clock skew and its impact on correctness and SLAs
- Logical and hybrid clocks (Lamport, vector, HLC) and when they matter
- Idempotency keys, deduplication windows, TTLs, and retry-safe semantics

Suggested reading order
1) 01-ids-fundamentals.md — goals, properties, and when to choose each family
2) 02-id-generation-strategies.md — centralized vs decentralized, collision math, and SLAs
3) 03-id-types-and-encodings.md — UUIDs, ULID/KSUID, base encodings, storage considerations
4) 04-ordering-sortability-and-sharding.md — making ordered IDs work without hotspots
5) 05-time-fundamentals-clocks-and-skew.md — wall vs monotonic, skew budgets, NTP/PTP
6) 06-logical-clocks-and-ordering.md — happens-before, causal ordering, and HLC
7) 07-event-time-processing-time-and-watermarks.md — streams and analytics semantics
8) 08-idempotency-dedup-and-ttls.md — retries, dedup windows, and key design
9) 09-operations-observability-and-runbooks.md — monitoring, skew alerts, collision SLOs
10) 10-selection-guide-and-comparisons.md — quick matrix to pick an approach
11) 11-case-studies.md — real systems and their trade-offs

Adjacent topics
- Consistency & CAP — Clocks and Ordering: ../05-consistency-and-cap/05-clocks-and-ordering.md
- Data Partitioning — Routing and Hotspot Mitigation: ../03-data-partitioning/01-routing-and-catalogs.md, ../03-data-partitioning/03-hotspot-mitigation.md
- Messaging & Streaming — Ordering and Offsets: ../07-messaging-and-streaming/02-topics-partitions-and-ordering.md
- Rate Limiting & Backpressure — Retries and Idempotency: ../08-rate-limiting-and-backpressure/05-retries-idempotency-and-client-behavior.md

References
- RFC 4122 (UUID), draft UUIDv7; ULID and KSUID specs; Twitter Snowflake posts
- Lamport clocks and Vector clocks papers; Hybrid Logical Clocks (HLC)
- NTP/chrony best practices and monitoring guides
