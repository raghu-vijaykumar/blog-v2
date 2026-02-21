---
title: Selection Guide and Comparisons — Choosing ID and Time Strategies
description: Decision matrix for selecting identifier types, issuance models, time synchronization, and dedup policies based on workload traits.
---

## Overview
This guide condenses the module into practical decision tables. Evaluate workload characteristics—throughput, ordering needs, human exposure, compliance—and map them to recommended ID schemes, encodings, clocks, and TTL strategies.

## What, Why, When (and when-not)
What
- Comparative heuristics for ID format, generation model, time sync method, and dedup approach.

Why
- Teams need quick guidance without re-reading full docs. A structured selection process speeds architecture reviews and design docs.

When
- Architecture kickoffs, migrations, due diligence for acquisitions, or audits requiring justification of identifier/time decisions.

When-not
- Do not rely solely on the table when designing novel systems with unique constraints—pair with deeper analysis.

## Core concepts and variants
- **Decision criteria**: Ordering strictness, collision tolerance, human readability, infrastructure maturity.
- **ID scheme options**: Random UUIDv4, ULID/UUIDv7, Snowflake-style, DB sequence, composite natural key.
- **Time sync options**: Public NTP, managed Chrony with fallback, PTP, GPS-backed TrueTime-like.
- **Dedup policies**: Stateless client tokens, server-side cache, durable store.
- **TTL tiers**: Short (minutes), medium (hours), long (days) based on retry patterns.

## Design decisions and trade-offs
- **Simplicity vs control**: Central sequences are easy but brittle; distributed schemes require more upfront design.
- **Compliance**: Financial audits may mandate deterministic ordering and traceable time sources.
- **Cost**: PTP hardware and multi-region dedup stores increase spend; justify via risk exposure.
- **Migration complexity**: Transitioning ID formats can require data backfills and dual-encoding periods.

## Algorithms/policies (conceptual)
- **Scoring matrix**
```pseudo
score(scheme) = w_order*order_score + w_scale*scale_score + w_human*human_score + w_ops*ops_score
select scheme with max score given weights from product requirements
```
- **Dual-write migration**
```pseudo
if migrating:
  store(old_id, new_id)
  publish mapping table
  cutover reads once coverage > 99.9%
```

## Architecture and components
- Decision template integrated into architecture review checklist.
- Reference catalog storing past decisions, rationale, and post-mortems for reuse.
- Automation to lint design docs ensuring selected schemes match documented criteria.

## Operational considerations
- Review selection yearly; business requirements change (new regions, compliance).
- Maintain runbooks for transitions (dual ID issuance, TTL adjustments).
- Track exceptions where teams deviate from guide and document risk acceptance.

## Examples
Example A (quantitative): Selection table

| Requirement | Preferred ID Type | Encoding | Dedup | Clock | Notes |
| --- | --- | --- | --- | --- | --- |
| Social feed, 200k rps, near-order | ULID/UUIDv7 | Base32 | Redis 24h TTL | Managed NTP | Hash suffix to avoid hotspots |
| Payment API, compliance-heavy | HLC-backed Snowflake | Base62 | Durable DB 7d TTL | Chrony + GPS | Strict audit trail |
| Analytics events, batch-friendly | UUIDv4 | Hex (binary storage) | Stateless key | Public NTP | Simplicity, no ordering |

Example B (architectural): Migration playbook
- Legacy DB sequences replaced with Snowflake IDs. During migration, write both `seq_id` and `snowflake_id`; new services consume Snowflake. After verifying parity and updating downstream ETL, retire sequence generator and backfill missing IDs via mapping table.

## Edge cases and anti-patterns
- Mixing multiple ID schemes for same entity without authoritative mapping causes referential drift.
- Selecting advanced time sync (PTP) without operational expertise leads to silent failure; start with managed NTP unless budget justifies.
- Skipping dedup due to low traffic often backfires as usage grows; plan ahead.

## Interactions with adjacent topics
- Data Partitioning — Catalog routing: ../03-data-partitioning/01-routing-and-catalogs.md
- Availability — Failover runbooks: ../09-availability-and-fault-tolerance/06-failover-promotion-and-dr.md

## Production checklist
- Document chosen ID/time strategies with rationale and review cadence.
- Implement guardrails to prevent unauthorized schema drift.
- Establish migration plan including dual writes and backfill approach.
- Share comparison table with onboarding engineers and SREs.

## Interview framing checklist
- Given workload X, justify ID type and dedup window.
- Describe how you’d migrate from auto-increment to Snowflake with zero downtime.
- Explain trade-offs between UUIDv4 and ULID in analytics vs feeds.

## References
- Stripe and Twitter architectural blogs on ID evolution.
- Martin Kleppmann’s “Designing Data-Intensive Applications” (chapters on replication/ordering).
- Google SRE worksheets on production readiness.

## Diagram
```mermaid
flowchart LR
  Requirements --> Matrix[Decision Matrix]
  Matrix --> Recommendation[Recommended Strategy]
  Recommendation --> Review[Architecture Review]
  Review --> Implementation[Implementation Plan]
  Implementation --> Feedback[Post-launch Feedback]
  Feedback --> Matrix
```
