# Proposal

## Why

All 22 classes live in one package `es.upm.park`, and `CommandProcessor` mixes command parsing with controller state and cross-entity domain rules while `App` wires file stores and manages the `--fly` directory. Presentation, business and persistence responsibilities must be physically separated without changing any observable behavior.

## What Changes

- Reorganize the code into layer packages: `es.upm.park.presentation` (terminal/command-file interaction, command parsing, HELP/EXIT), `es.upm.park.business` (controllers and domain rules) with `es.upm.park.business.model` (domain objects), and `es.upm.park.persistence` (file stores and data-directory handling).
- Split the current `CommandProcessor`: parsing and output stay in presentation; the controller state (system date, price) and cross-entity rules move to a new business controller `ParkController`, which also assembles the managers and stores for a data directory (normal or `--fly`).
- No class in the Java default package; command syntax, messages, file formats and E1/E2 behavior unchanged; `mvn package` still produces the executable JAR (manifest main class updated).
- `uml/model.puml` updated to the new packages and classes so the UML model stays consistent with the code.

Mandatory milestone message from the source: `[F3-C01] Reorganize application into layered packages`.

## Capabilities

### New Capabilities
- `application-layering`: Layer packages, allowed dependency directions and responsibility boundaries.

### Modified Capabilities
- None (all command, persistence and model requirements remain as specified; this change adds structural requirements only).

## Impact

- All production and test classes move to layer packages; `CommandProcessor` is split into presentation `CommandProcessor` + business `ParkController`; `App` no longer constructs stores or manipulates directories.
- `pom.xml` manifest main class becomes `es.upm.park.presentation.App`.
- `uml/model.puml` updated. No data-format or command changes.
