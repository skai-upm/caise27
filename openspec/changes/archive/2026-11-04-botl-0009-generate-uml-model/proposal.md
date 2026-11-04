# Proposal

## Why

The repository's PlantUML model (`docs/uml/model.puml`) was written incrementally. It lists methods, mixes in usage arrows and technical labels, and lives outside the required location. The project needs one faithful class diagram, derived from the current code, at `uml/model.puml`, following the stated notation rules.

## What Changes

- Generate `uml/model.puml` from the current Java sources and remove the superseded `docs/uml/model.puml`, so the project has a single model.
- What the diagram shows:
  - every current class, abstract class and enum;
  - inheritance (`Ticket` ← `IndividualTicket`, `GroupTicket`);
  - associations, aggregation (`GroupTicket` → `Group`) and composition (`GroupTicket` → constituent `IndividualTicket`), with navigability crosses and multiplicities.
- Notation rules:
  - each relationship is named after the holding attribute;
  - attributes are restricted to primitive/value types and enums, never object references;
  - no methods, no technical labels;
  - usage arrows appear only for classes that have no incoming relationship.
- No inverse relationships are added and no class is invented.
- No production code changes. A test is added that keeps the diagram's class list in sync with `src/main/java`.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `uml-model`: "PlantUML class model" moves to `uml/model.puml` and follows the notation rules above. Methods are no longer shown, which replaces the earlier `getHolders()` scenario.

## Impact

- Files: new `uml/model.puml`, deleted `docs/uml/model.puml`, new `UmlModelTest`.
- No behaviour change: the full test suite must pass before and after.
