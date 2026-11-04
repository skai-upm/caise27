# Design

## Context

Current state: package `com.parque` with `App` (input modes, `--fly` directory, wiring), `CommandProcessor` (parsing, formatting, and also date/price ownership and cross-entity rules), model classes, services, `UserValidator` and five repositories. Tests are all in `com.parque` and rely on some package-private members (`App.run`, `App.createFlyDirectory/deleteFlyDirectory`, `CommandProcessor.COMMAND_SYNTAX`).

## Goals / Non-Goals

**Goals:** physical separation by package with explicit dependency rules; the main controller in business; identical behaviour.

**Non-Goals:** new commands or rules; repository interfaces / dependency inversion (later F3 changes deal with patterns and injection); changing file formats.

## Decisions

- **Package map** (see proposal). `App` remains `com.parque.App` as the composition root so the executable JAR's `Main-Class` changes only by not moving (`com.parque.App` unchanged in `pom.xml`).
- **Split of `CommandProcessor`**:
  - `business.controller.ParkController` — fields: the four services, `currentDate`, `ticketPrice`. Methods mirror the operations: `setSystemDate`, `setTicketPrice`, `createUser`, `deleteUser` (ticket check + group cascade), `updateUser`, `createGroup`, `addGroupMember`/`removeGroupMember`/`deleteGroup` (lock check), `createTicket`, `deleteTicket`, `createGroupTicket`, `deleteGroupTicket`, `createAttraction`, `enterAttraction`, `exitAttraction`, `attraction(id)`. Static factory `open(Path dataDir)` wires repositories and services (business → persistence).
  - `presentation.CommandProcessor` — splits the line, checks parameter counts, parses dates (`yyyy-MM-dd`) and prices, calls the controller and formats `OK…`/`ERROR:` responses, HELP and STATUS. Convenience constructors taking the services are kept (they build a `ParkController`), and `getCurrentDate()`/`getTicketPrice()` delegate, so existing tests keep their shape.
  - `presentation.CommandRunner` — the shared line loop for standard input and command files, including the controlled file-error messages.
- **`persistence.DataDirectory`** — `createTemporary(base, start)` and `delete(dir)` for `--fly` (moved from `App`).
- **Allowed dependencies**: `App` → all layers (composition root); presentation → business; business.controller → business.service, business.model, persistence (only in `open`); business.service → business.model, persistence; persistence → business.model; business.model → nothing in the project. Enforced by `LayeringTest`, which scans the `import` lines and fully-qualified references of each source file.
- **Mechanics**: files are moved with `git mv`; package declarations and imports are rewritten; package-private members needed across packages become `public` (no behaviour change). Tests move to the package of the class under test.
- **Equivalence check**: a scripted run covering all E1/E2 commands is executed with the JAR built before the change and after it, on fresh directories; outputs and data files are compared byte-for-byte.

## Risks / Trade-offs

- [Package cycle business.service ↔ persistence through the model] → accepted: persistence depends on `business.model`, services on repositories; there is no class-level cycle and no dependency from model to persistence.
- [Large mechanical diff] → done with a script and verified by the full test suite plus the before/after equivalence run.
