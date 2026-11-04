# Proposal

## Why

All 21 classes live in one package, `es.upm.etsisi.amusementpark`. `CommandProcessor` mixes three concerns: command parsing and output formatting (presentation), the run-wide system date and ticket price, and cross-entity rules such as the user-deletion check and the group lock (control/business). Separating presentation, business and persistence makes the responsibilities explicit before Delivery 3 adds more features, and it must not change any observable behaviour.

## What Changes

- Reorganise the code into layer packages under `es.upm.etsisi.amusementpark`:
  - `presentation`: command reading, parsing, dispatch and output (`CommandRunner`, `CommandProcessor`)
  - `business.controller`: a new `ParkController` that owns the system date and ticket price and performs the cross-entity operations now embedded in `CommandProcessor`
  - `business.service`: the managers with the domain rules (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`)
  - `business.model`: entities, the enum, the validator and the rejection exception
  - `persistence`: the five file repositories
  - the root package keeps only the entry point `App`, which wires the layers (Main-Class unchanged)
- No class in the Java default package (already true; kept as a requirement).
- Command syntax, outputs, data file formats and E1/E2 behaviour are unchanged. Tests move with their classes, and a new architecture test checks the layer boundaries.
- The PlantUML model shows the packages.

## Capabilities

### New Capabilities
- `application-layering`: layer packages, allowed dependencies, and behaviour preservation across the refactor.

### Modified Capabilities
_None._ All existing requirements keep their behaviour, so this change has no delta for them.

## Impact

- Every production and test source file moves package. Fully qualified class names change, and the JAR entry point `es.upm.etsisi.amusementpark.App` stays.
- New `ParkController`. `CommandProcessor` now depends on it instead of the four managers.
- No new dependencies and no data or command changes.
- Delivery milestone message: `[F3-C01] Reorganize application into layered packages`.
