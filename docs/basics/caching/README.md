# Caching

## Why Caching Matters

Caching is a fundamental technique in system design that dramatically improves performance, reduces latency, and optimizes resource usage. In real-world systems, caching prevents redundant computations, minimizes database load, and enables applications to handle millions of requests per second. Misunderstanding caching leads to inefficient systems, data inconsistencies, and poor user experiences.

## Learning Philosophy

This repository teaches caching through concepts-first approach:
- **Concepts drive structure**: Each idea lives in its own folder
- **Code validates understanding**: Tests prove conceptual mastery
- **Progressive complexity**: From intuition to advanced trade-offs
- **Test-as-truth**: If tests fail, the concept isn't understood yet

## Learning Path

| Concept                                                        | Focus                               | Duration |
| -------------------------------------------------------------- | ----------------------------------- | -------- |
| [00 – Mental Model](00-mental-model/README.md)        | What is caching? Why does it exist? | 15 min   |
| [01 – Basics](01-basics/README.md)                    | Core caching operations             | 30 min   |
| [02 – Real-world Usage](02-real-world/README.md)      | Common patterns and scenarios       | 45 min   |
| [03 – Internals & Trade-offs](03-internals/README.md) | How caches work under the hood      | 60 min   |
| [04 – Advanced Patterns](04-advanced/README.md)       | Sophisticated caching strategies    | 60 min   |
| [05 – Failure Modes](05-failures/README.md)           | What breaks and how to fix it       | 45 min   |

## How to Learn

1. Start with [00 – Mental Model](00-mental-model/README.md)
2. Read the concept explanation
3. Run the code examples
4. Execute the tests to validate understanding
5. Move to the next concept

## Validation

Run `powershell -ExecutionPolicy Bypass -File run-all.ps1` to validate your understanding across all concepts.

**Expected output:**
```
✅ Passed: 00-mental-model
✅ Passed: 01-basics
✅ Passed: 02-real-world
✅ Passed: 03-internals
✅ Passed: 04-advanced
✅ Passed: 05-failures

🎉 All concepts validated successfully!
```

If any test fails, revisit that concept - the tests encode the invariants you must understand.

## Prerequisites

- Java 11+ and Maven (for Java examples)
- Python 3.8+ and pip (for Python examples)
- Basic understanding of data structures

## Repository Structure

```
understanding-caching/
├── README.md                 # This file - navigation & overview
├── run-all.sh                # One-command validation
└── concepts/                 # Concept-driven organization
    ├── 00-mental-model/      # Intuition first
    ├── 01-basics/           # Core operations
    ├── 02-real-world/       # Practical applications
    ├── 03-internals/        # How it works
    ├── 04-advanced/         # Complex strategies
    └── 05-failures/         # What goes wrong
```

Each concept folder contains:
- README.md: Concept explanation
- java/: Java implementation and tests
- python/: Python implementation and tests

## Teaching Approach

- **Concept-first**: Ideas before implementation
- **Executable learning**: Run code, see results
- **Test-driven understanding**: Prove concepts through assertions
- **Language-agnostic**: Same ideas in Java and Python
- **Progressive disclosure**: Complexity revealed gradually

Ready to begin? Start with [00 – Mental Model](00-mental-model/README.md).
