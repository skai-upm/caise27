# Design

## Context

All Delivery 2 milestones (F2-C01..F2-C09) are implemented. HELP shows the Delivery 1 user commands with the Delivery 1 placeholders (`<firstName>`, `<email>`, `<value>`…), while the Delivery 2 source lists them with `<FirstName>`, `<Email>`, `<NewValue>`… as the minimum syntax.

## Goals / Non-Goals

**Goals:**
- Contractual HELP text; integration evidence; restriction checks; from-scratch build.

**Non-Goals:**
- New features.

## Decisions

- **HELP text:** replace the three user lines with the Delivery 2 forms (Delivery 2 explicitly redefines the HELP minimum, so the Delivery 1 requirement is modified rather than both forms printed). Syntax error messages reuse the same strings.
- **`Delivery2IntegrationTest`:** one command file covering every Delivery 2 command plus rejections; run in normal mode (checked line by line and for persisted data) and with `--fly` (same output, no data left, normal data untouched).
- **`ModelStructureTest`:** reflection over declared fields to assert the relationship directions and the ticket hierarchy.
- **Git history (flagged):** all mandatory Delivery 2 commits exist with exact text and in roadmap order; the Delivery 1 `[F1-C08]` order deviation noted in F1-C09 remains.
- **Mandatory commit:** `[F2-C10] Complete delivery 2 integration` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Changing HELP placeholders] → Placeholder names only; command syntax and behaviour are unchanged.
