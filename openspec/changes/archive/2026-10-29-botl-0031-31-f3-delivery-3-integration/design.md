# Design

## Context

All Delivery 3 features are implemented (F3-C01…F3-C09). See proposal.md.

## Goals / Non-Goals

**Goals:** prove coexistence end to end; document; audit.
**Non-Goals:** new features.

## Decisions

- **`DeliveryThreeIntegrationTest`** (presentation package) drives `App.execute` with a combined session file, a restart on the same directory and a `--fly` run, asserting key responses, totals and data files.
- **Audit by grep**: dependencies, layering imports, singleton constructors (already tested), regex/serialization APIs.
- **README** rewritten for Delivery 3.

## Risks / Trade-offs

- None.
