# Design

## Context

All Delivery 3 milestones (F3-C01..F3-C09) are implemented and individually tested (architecture, DI, generics, builder, BCrypt, pass persistence). HELP is generated from `CommandCatalog`.

## Goals / Non-Goals

**Goals:**
- End-to-end evidence across the three deliveries; HELP completeness; restrictions; delivered JAR.

**Non-Goals:**
- New features.

## Decisions

- **`Delivery3IntegrationTest`:** one `input_fase3.txt`-style session covering every Delivery 3 command plus rejections, asserted line by line; the same input with `--fly` must produce identical output and leave no data; a second normal run after the first verifies persisted consumed passes (consumed Special Pass still blocks a second special entry and a second purchase).
- **HELP completeness:** test asserts every Delivery 3 syntax from the source list plus all Delivery 1/2 syntaxes appear, and that HELP EXTENDED has a description for each.
- **Restrictions:** existing `ArchitectureTest`, `DependencyInjectionTest`, `AttractionTest` (bounded generics), `InvoiceBuilderTest`, `UserServiceTest` (BCrypt) cover them; `ModelStructureTest` gains a check that `Pass` has no way to be deleted on consumption (consumption only flips a flag) — covered by the integration persistence check instead of reflection.
- **Git history (flagged):** all Delivery 3 mandatory commits exist with exact text and in order; `[F3-C07] Add generic attraction access types` is the corrective commit `82ece91` that follows the implementation commit `6d658ab` (whose subject lacked "access"). Delivery 1's `[F1-C08]` order deviation remains as reported.
- **Mandatory commit:** `[F3-C10] Complete delivery 3 integration` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Long session test duration due to BCrypt] → A handful of users only.
