---
title: Query Processing and Result Assembly
---

# Query Processing and Result Assembly

## Overview
Query processing interprets user input, retrieves relevant candidates, then assembles results with explanations, highlights, and aggregations. The goal is fast, forgiving search that adapts to user feedback while maintaining correctness and consistency.

## Query Understanding
- **Normalization**: case folding, Unicode normalization, punctuation handling, token segmentation.
- **Spell correction & autosuggest**: edit-distance models, noisy channel approaches, ML-based correctors using query logs.
- **Synonyms & expansions**: curated dictionaries, dynamic term co-occurrence mining, paraphrase embeddings.
- **Intent detection**: classifiers for query type, locale, units, or domain-specific filters.
- **Structured parsing**: detect filters (price:`<50`), entity types, natural language question answering triggers.

## Retrieval and Filtering
- Apply analyzers to tokens; route to relevant indices or shards.
- Combine lexical/dense retrieval with filters (permissions, availability) and business rules.
- Use query routing heuristics for multi-tenant or vertical search (route to news index for “latest” queries).

## Result Assembly
- **Highlighting**: term vectors or postings positions to show matching snippets; fallback to summary fields.
- **Aggregations/facets**: hierarchical or flat facets, numeric histograms, date ranges. Manage caching and precomputation.
- **Explanations**: expose score components (BM25, boosts, business rules) for debugging and editorial control.
- **Related queries & suggestions**: surface popular reformulations, similar items, co-clicked content.

## Pagination and State
- Maintain stable ordering across pagination by including sort keys (score, doc ID, timestamp).
- Use cursor-based pagination (`search_after`, `scroll`, search sessions) for deep navigation; guard against stale cursors during rebalances.
- Track query/session IDs for analytics and personalization.

## Operational Considerations
- Enforce query timeouts, circuit breakers, and fallback flows (return partial results, top matches).
- Cache frequently repeated queries and top facets; invalidate on schema/index changes.
- Monitor query mix (top intents, zero results, high-latency queries); adapt infrastructure accordingly.
- Record query parsing outcomes and errors for continuous improvement.

## Checklist
- [ ] Query parsing and normalization pipelines versioned and tested.
- [ ] Autosuggest/spell correction models evaluated for precision/recall and latency.
- [ ] Aggregation performance tuned (pre-computed caches, filters, memory budgets).
- [ ] Score explanations accessible to support/merchandising teams.
- [ ] Query/session analytics feeding relevance tuning workflows.
