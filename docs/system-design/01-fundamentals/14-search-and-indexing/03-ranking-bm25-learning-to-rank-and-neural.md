---
title: Ranking - BM25, Learning to Rank, and Neural Re-Ranking
---

# Ranking: BM25, Learning to Rank, and Neural Re-Ranking

## Overview
Ranking transforms retrieved candidates into an ordered list optimized for user satisfaction. Effective pipelines layer lexical scoring with heuristics, machine learning, and neural models, balancing offline evaluation rigor with online experimentation.

## Ranking Stages
- **Stage 0: Candidate generation** — BM25/top-K lexical retrieval, ANN dense vector search, category filters. Aim for high recall at reasonable cost.
- **Stage 1: Scoring & feature extraction** — compute document/query features (term frequency, BM25, freshness, embeddings similarity, click priors, business rules).
- **Stage 2: Re-ranking** — apply learning-to-rank (LTR) models (gradient boosted trees, LambdaMART) or neural cross-encoders. Include calibration layers for score combinations.
- **Stage 3: Post-processing** — diversity/novelty filters, deduplication, hard business rules (compliance, region restrictions), result grouping.

## Lexical Baselines
- **BM25 / BM25F**: term frequency saturation with document length normalization; BM25F adds field weights. Tune parameters (`k1`, `b`) per index and field.
- **DFR / Language Models**: Divergence from Randomness, Dirichlet-smoothed language models; alternative baselines when corpora are specialized.
- **Query-independent boosts**: recency, popularity, quality scores applied multiplicatively or additively.

## Learning to Rank (LTR)
- **Pointwise vs. pairwise vs. listwise**: regression on relevance scores, preference pairs (LambdaRank), or listwise metrics (ListNet, Soft-NDCG).
- **Feature stores**: maintain versioned offline stores and real-time feature pipelines for latency-critical signals.
- **Judgment data**: curated human labels, click models (DBN, DCM), counterfactual learning (IPS, SNIPS). Balance bias and coverage.
- **Experimentation**: offline evaluation (NDCG@K, ERR@K) gates online A/B or interleaving tests. Monitor long-term engagement and fairness metrics.

## Neural Re-Ranking
- **Bi-encoders vs. cross-encoders**: bi-encoders score via embedding dot products (fast); cross-encoders jointly encode query-doc pairs (accurate but expensive). Use distillation to bridge gaps.
- **Late interaction models**: ColBERT, SPLADE, hybrid sparse/dense features that keep token-level semantics while remaining efficient.
- **Caching**: precompute document embeddings; apply ANN search with dynamic routing to accelerate inference.
- **Latency controls**: apply neural re-ranking only to top N (e.g., 100) candidates; use hardware acceleration (GPU, vector cores) and batching.

## Design Decisions and Trade-offs
- **Explainability vs. accuracy**: tree-based LTR models easier to debug; neural rankers require tooling for introspection. Provide explanations for support teams.
- **Freshness signals**: short half-life content needs dynamic features (time since publish). Ensure features update without reindexing entire corpus.
- **Diversity**: guard against winner-takes-all by applying result diversification (xQuAD, MMR) for ambiguous queries.
- **Fairness and bias**: monitor for feedback loops favoring already-popular items; apply exploration/exploitation strategies.

## Operational Considerations
- Keep ranking configs versioned and deployable separately from index schema.
- Establish rollback and fallbacks (pure BM25, rules-only) for model regressions.
- Log score breakdowns per result for debugging; build dashboards for click-through rate, dwell time, abandonment.
- Automate feature quality checks (distributions, drift) and model monitoring (calibration, latency, resource consumption).

## Checklist
- [ ] Baseline BM25 tuned with reliable evaluation sets.
- [ ] LTR/neural models trained with unbiased judgments or debiased click logs.
- [ ] Stage-wise pipeline with fallbacks and guardrails for latency.
- [ ] Score explainability tooling available to support and merchandising teams.
- [ ] Experimentation framework for continuous relevance tuning.
