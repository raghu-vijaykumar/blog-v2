---
title: Pagination, Sorting, and Navigation
---

# Pagination, Sorting, and Navigation

## Overview
Pagination and sorting policies shape how users explore search results and how efficiently the system serves deep requests. Thoughtful design balances usability, performance, and fairness while preventing abuse.

## Pagination Patterns
- **Offset/limit**: simplest but expensive for deep pages; requires rescoring skipped results.
- **Cursor-based (`search_after`)**: stateless pagination using stable sort keys; resilient to index updates.
- **Scroll contexts**: maintain server-side state (Elasticsearch scroll, point-in-time) for consistent batch processing.
- **Infinite scroll**: client-driven incremental fetch; monitor for abandonment and resource leaks.

## Sorting Strategies
- **Primary sort**: relevance score or business ranking.
- **Secondary sort**: tie-break on deterministic fields (doc ID, publish date) to ensure stability.
- **User-selected sorts**: price, rating, newest. Precompute field data or doc values to avoid heavy recomputation.
- **Fairness-aware reordering**: rotate results to meet exposure constraints or marketplace policies.

## Faceting and Navigation
- Provide facets aligned with user goals (category, brand, price ranges).
- Use hierarchical facets for nested taxonomies; track depth budget to avoid overly complex UI.
- Precompute facet counts via aggregations; cache frequently accessed facet combinations.
- Support drill-down (filter-in) and drill-up (breadcrumb) patterns.

## Performance Considerations
- Limit deep paging (e.g., cap at 10k results) and direct power users to exports or APIs.
- Enforce per-query timeouts; return partial results when necessary.
- Compress sorted field data, enable doc values, and manage heap usage.
- Guard against bots/brute-force crawling with rate limits and query cost accounting.

## Operational Considerations
- Monitor long-tail pagination usage and identify abuse (cart scrapers, bots).
- Log sort/pagination preferences to guide UI improvements.
- Keep analytics on facet selections and zero-result filters to refine schema.
- Ensure pagination cursors remain valid across rebalances or fail gracefully.

## Checklist
- [ ] Pagination strategy documented per surface (web, API, internal tools).
- [ ] Sort keys stable and consistently applied across replicas.
- [ ] Deep paging safeguards (limits, warnings) implemented.
- [ ] Facet caching strategy tuned for top combinations.
- [ ] Bot detection and rate limiting integrated with pagination endpoints.
