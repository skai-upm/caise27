# Design

## Context

See proposal.md – Why. Current packages: `es.upm.park` (Main), `command`, `controller`, `model`, `service`, `repository`, `persistence`. Exceptions: `ParkException` (base, in `service`) with `User/Group/Ticket/AttractionException`; `PersistenceException` in `persistence`; `CommandProcessor` catches them per command plus a `RuntimeException` safety net.

## Goals / Non-Goals

**Goals:**
- Pure move refactor with layer-aligned packages and a guard test.

**Non-Goals:**
- Renaming classes or changing messages.

## Decisions

- **Mapping**: `Main`, `command.*` → `presentation`; `model.*` → `business.model`; `service.*` → `business.service`; `controller.*` → `business.controller`; `repository.*` → `persistence.repository`; `persistence.*` stays. Tests follow their classes (package-private test access is kept).
- **Business → persistence dependency** (services use repositories, the controller wires stores from a data directory) is the conventional downward dependency; persistence depends on `business.model` only for the entities it stores.
- **`DataDirectory` use in `Main`**: `Main` selects the mode and passes the directory to `ParkController.start`; it never reads or writes data files itself, which stays within the rule.
- **Exception policy**: unchanged hierarchy (business `ParkException` subclasses per element, `PersistenceException` for I/O); handling is centralized in `CommandProcessor.process` (presentation). Exceptions are thrown where the rule is violated (services, controller, validators), not created for printing.
- **`ArchitectureTest`**: scans `src/main/java` sources, checks `package` declarations against the three layer roots, and checks forbidden `import` prefixes per layer.
- **Move tooling**: `git mv` per directory plus rewriting `package`/`import` declarations; `pom.xml` `mainClass` → `es.upm.park.presentation.Main`.
