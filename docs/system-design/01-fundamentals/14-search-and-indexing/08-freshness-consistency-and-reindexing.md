---
title: Freshness, Consistency, and Reindexing
---

# Freshness, Consistency, and Reindexing

## Overview
Search relevance depends on timely, consistent data. Managing freshness involves controlling indexing latency, handling deletions and updates, and planning reindex operations that minimize downtime.

## Freshness Dimensions
- **Indexing latency**: time from source update to searchable document. Govern via refresh intervals, pipeline delays, and replica sync.
- **Visibility latency**: propagation delay for ranking signals (clicks, ratings) and derived features.
- **Staleness tolerance**: define acceptable windows by content type (e.g., minutes for news, hours for documentation).

## Consistency Models
- **Eventual consistency**: default for distributed search clusters; replicas catch up asynchronously. Mitigate via read-your-writes features or targeted refresh.
- **Near-real-time consistency**: low refresh intervals provide fast visibility at increased resource cost.
- **Strong consistency**: rarely needed; emulate via transactional writes to single-shard systems or query-time fallback to primary data store.

## Deletions and Updates
- Use versioned writes (optimistic concurrency) to avoid lost updates.
- Soft-deletes with tombstone flags ensure deletes propagate before document removal.
- For compliance deletions (Right to be Forgotten), propagate delete events with strict auditing and confirm replica purge via monitoring.

## Reindexing Strategies
- **Alias swap / blue-green**: index into new version, validate, then atomically switch alias.
- **Rolling reindex**: reindex shard-by-shard while keeping system online; requires compatibility between versions.
- **Shadow indexes**: build new index under low load; replay missing updates before cutover.
- **Partial reindex**: filter-based reindex for affected documents (e.g., schema change limited to subset).

## Operational Considerations
- Schedule reindex during low-traffic windows; throttle to maintain query SLAs.
- Monitor refresh and flush metrics, translog size, merge backlog.
- Keep snapshots and backups to recover from failed reindex; test restore procedures.
- Automate verification (document counts, sample relevancy checks) before alias swap.

## Checklist
- [ ] Freshness SLAs defined per content type and tracked in dashboards.
- [ ] Delete/update workflows tested, including compliance-driven purges.
- [ ] Reindex playbooks documented with rollback plans and verification steps.
- [ ] Backups and snapshots validated for recovery scenarios.
- [ ] Monitoring in place for refresh latency, replication lag, and merge pressure.
