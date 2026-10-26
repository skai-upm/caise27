# Proposal

## Why

The hand-maintained diagram in `docs/uml/model.puml` mixes methods, usage arrows and relationship-typed attributes, so it does not read as a faithful structural model. The course requires a PlantUML class diagram, derived from the actual code, at `uml/model.puml`, following precise notation rules.

## What Changes

- Analyse the current implementation and write `uml/model.puml` (valid PlantUML) documenting it exactly as it is: classes, the `Admission` interface, the `UserField` enum, the ticket hierarchy (`Ticket` ← `IndividualTicket`, `GroupTicket`), inheritance/realisation, associations, aggregation and composition with direction and multiplicities.
- Notation rules: no methods; no usage relationships except to show how a class with no incoming relationship is used; attributes only of primitive/value and enum types (references to other classes appear only as relationships); no technical labels (`{readOnly}`, `{static}`, stereotypes) on attributes; relationship names equal the holding attribute's name; association lines carry a cross at the holder's end and an arrow at the referenced class; no inverse relationships that do not exist in the code.
- Replace `docs/uml/model.puml` by `uml/model.puml` (single source of truth).
- **No functional or refactoring changes** to the Java code.

## Capabilities

### New Capabilities

### Modified Capabilities
- `domain-model`: model location becomes `uml/model.puml` and the diagram must follow the notation rules above while faithfully reflecting the implementation.

## Impact

- New `uml/model.puml`; `docs/uml/model.puml` removed (moved).
- No Java source, test or data format changes.
