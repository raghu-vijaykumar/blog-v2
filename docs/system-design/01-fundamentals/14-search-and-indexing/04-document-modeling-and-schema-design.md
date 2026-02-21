---
title: Document Modeling and Schema Design
---

# Document Modeling and Schema Design

## Overview
Search documents are denormalized projections of source entities shaped to support recall, ranking, and analytics. Schema design must balance flexibility, storage efficiency, relevance, and operational safety.

## Document Construction
- **Entity aggregation**: combine relational records, rich media metadata, and permissions into a single document per search entity (product, article, user profile).
- **Field types**: textual, keyword, numeric, boolean, date, geo; vector fields for embeddings; nested objects for hierarchical facets.
- **Metadata**: store necessary filtering and boosting attributes (category, price, popularity). Avoid overloading with high-cardinality tracking ids.
- **Permissions & tenancy**: embed access control lists (ACLs) or tenant identifiers; consider filter contexts vs. document-level security wrappers.

## Schema Evolution
- **Versioned mappings**: maintain schema versions with migration playbooks. Use alias switching or index-level version numbers to rollout.
- **Backward compatibility**: add new fields as optional; avoid deletions without a full reindex plan.
- **Dynamic mappings**: convenient but risky; prefer explicit mappings for critical fields to prevent type explosions.
- **Multi-language fields**: store per-language subfields or separate documents per locale for better analyzers.

## References and Relationships
- **Denormalization vs. joins**: search engines lack transactions; denormalize to avoid joins at query time. Use parent-child or nested docs when updates are frequent and consistent relationships are critical.
- **Cross-index lookups**: use application-level joins or secondary services for complex relationships (e.g., user permissions stored elsewhere).

## Operational Considerations
- Track field usage; retire unused fields to lower index size.
- Validate document size; large payloads (binary blobs) should move to object storage with references.
- Monitor schema drift between environments; enforce CI checks on mappings.
- Keep sample documents for debugging analyzers and ranking features.

## Checklist
- [ ] Defined search entities and denormalized fields covering key use cases.
- [ ] Versioned schema/mapping with changelog and rollback plan.
- [ ] Access control strategy (document filtering, field redaction) documented and tested.
- [ ] Index size and field statistics monitored for regressions.
- [ ] Schema updates integrated with reindex and deployment pipelines.
