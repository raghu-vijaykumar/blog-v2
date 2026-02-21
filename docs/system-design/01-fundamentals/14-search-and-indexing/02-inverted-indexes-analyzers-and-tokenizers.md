---
title: Inverted Indexes, Analyzers, and Tokenizers
---

# Inverted Indexes, Analyzers, and Tokenizers

## Overview
Search engines rely on inverted indexes that map terms to posting lists of documents and metadata. Analyzers tokenize, normalize, and enrich text before indexing; their choices determine recall, precision, and storage footprint. This section covers lexical retrieval foundations plus emerging hybrid setups that combine sparse and dense features.

## Core Structures
- **Dictionary**: sorted list or Finite State Transducer (FST) of unique terms mapping to postings lists. Supports prefix/suffix queries and efficient memory usage via shared prefixes.
- **Postings list**: stores document IDs, term frequency, positions, and optional payloads (boosts, norms). Position data enables phrase queries and proximity scoring.
- **Fielded documents**: each document comprises multiple fields (title, body, tags) with independent analyzers and field-level boosts.
- **Skip lists / block max indexes**: accelerate scoring by skipping segments based on term statistics (BM25 upper bounds) and enabling top-K algorithms like WAND/BMW.

## Analyzers and Token Pipelines
- **Tokenization**: whitespace, Unicode segmentation, language-specific rules (ICU). Specialized tokenizers handle CJK, agglutinative languages, emoji, and code identifiers.
- **Normalization**: lowercasing, ASCII folding, NFKC normalization, stopword removal, stemming/lemmatization. Over-normalization risks conflating distinct terms.
- **Filters**: synonyms (one-to-many expansions), shingles (n-grams), word delimiters, edge n-grams for autocomplete, char filters for HTML stripping.
- **Custom analyzers**: pipelines tuned per field (e.g., keyword analyzer for IDs, analyzer with shingles for titles, embeddings for semantic vectors).

## Hybrid Retrieval
- **Dense vectors**: sentence or document embeddings stored in approximate nearest-neighbor (ANN) indexes (HNSW, IVF-PQ, ScaNN). Provide semantic recall; complement lexical signals.
- **Token + vector fusion**: retrieval stage merges BM25 scores with cosine similarity via Reciprocal Rank Fusion (RRF) or learned blending weights.
- **Field-aware embeddings**: combine dense representations with metadata features (category, freshness) via sparse vector fields or product quantization.

## Design Decisions and Trade-offs
- **Analyzer symmetry**: query vs. index-time analyzers must align; synonyms can be applied asymmetrically (expand at index-time to increase recall, at query-time to minimize index bloat).
- **Position storage**: enabling positions elevates storage and memory; disable for fields that never support phrase search.
- **Stopwords**: removing reduces index size but may harm exact phrase matching (“To Be or Not to Be”); consider marking as low-importance instead of deleting.
- **Language detection**: per-document or per-field language identification allows selecting language-appropriate analyzers; fallback to multilingual analyzers when detection is uncertain.

## Operational Considerations
- Capture analyzer configuration in versioned schemas to guarantee reproducibility and facilitate reindexing.
- Test analyzers with corpora-specific samples; maintain regression suites for tokenization, synonyms, and stemming edge cases.
- Monitor term growth, posting list lengths, and heap usage; adjust index merge policies and memory budgets accordingly.
- Use warmers or query templates to load FSTs and lexicons into cache before releasing new shards to production.

## Checklist
- [ ] Define analyzer/test coverage per field including locale variants.
- [ ] Document index-time vs. query-time synonym handling and versioning.
- [ ] Establish metrics for postings size, segment counts, and cache hit ratios.
- [ ] Plan reindex playbooks when analyzers change.
- [ ] Evaluate hybrid retrieval benefits via offline/online experiments before production rollout.
