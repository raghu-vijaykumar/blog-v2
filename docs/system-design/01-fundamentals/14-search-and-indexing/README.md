---
title: Search & Indexing
description: Architect, operate, and tune search platforms that balance relevance, freshness, and performance across structured, semi-structured, and unstructured content.
---

# Search & Indexing

Deliver ranked, trustworthy answers from growing corpora. This module covers query understanding, indexing pipelines, ranking models, and the operational practices required to keep search responsive, relevant, and affordable as datasets and traffic scale.

What you will learn
- Search mental models: recall vs. precision, navigational vs. exploratory queries, qualitative evaluation loops.
- Index data structures: inverted indexes, postings lists, fielded documents, analyzers, token filters, embeddings, and hybrid retrieval.
- Ranking and relevance: BM25 variants, learning-to-rank, neural re-ranking, business signals, and evaluation frameworks.
- Document modeling and pipelines: denormalized docs, schema evolution, change data capture, near-real-time indexing, and background reindex jobs.
- Query execution: parsing, rewriting, aggregation, pagination, faceting, synonyms, typo tolerance, and result explanation.
- Operations and governance: multi-tenant clusters, scaling, routing, shard design, observability, cost management, and incident response.

Suggested reading order
1. 01-search-mental-models-and-use-cases.md — when and why to add search, user intents, and recall vs. precision framing.
2. 02-inverted-indexes-analyzers-and-tokenizers.md — core index structures, analyzers, stemming, normalizers, and embeddings.
3. 03-ranking-bm25-learning-to-rank-and-neural.md — scoring pipelines from lexical to neural and blending business signals.
4. 04-document-modeling-and-schema-design.md — field design, denormalization strategies, schema migrations, and metadata.
5. 05-indexing-pipelines-and-ingestion.md — CDC, batching, streaming, near-real-time updates, and reindexing tactics.
6. 06-query-processing-and-result-assembly.md — parsing, rewriting, aggregations, highlights, and result shaping.
7. 07-pagination-sorting-and-navigation.md — deep paging, search after, cursor APIs, faceting, and suggestions.
8. 08-freshness-consistency-and-reindexing.md — latency budgets, consistency trade-offs, soft deletes, and rebuild workflows.
9. 09-scaling-distribution-and-multi-tenancy.md — sharding, replicas, routing, ILM policies, and capacity planning.
10. 10-search-platform-ecosystem-and-selection.md — Elastic/OpenSearch, Solr, Postgres FTS, managed services, and hybrid stacks.
11. 11-operations-observability-and-relevance-tuning.md — instrumentation, error budgets, offline judgments, and continuous tuning.
12. 12-search-case-studies-and-patterns.md — real-world architectures, incident postmortems, and migration lessons.

Adjacent topics
- [Data Partitioning → Routing and Catalogs](../03-data-partitioning/01-routing-and-catalogs.md)
- [Databases & Storage → Columnar Stores](../06-databases-and-storage/README.md)
- [Messaging & Streaming → Change Data Capture](../07-messaging-and-streaming/README.md)
- [Caching → Results caching and warming](../01-caching/README.md)
- [Observability → SLOs and error budgets](../11-observability/README.md)

References
- Manning, Raghavan, Schütze — Introduction to Information Retrieval (lexical search fundamentals).
- Elastic, OpenSearch, Solr documentation (indexing, cluster management, relevance APIs).
- Microsoft/Baidu/Google research on learning-to-rank and neural re-ranking (BM25 vs. LTR vs. transformers).
- LinkedIn Galene, Slack search, and Reddit search engineering blogs (operational and architectural case studies).
- ACM SIGIR tutorials and TREC papers (evaluation methodology, benchmarks, test collections).
