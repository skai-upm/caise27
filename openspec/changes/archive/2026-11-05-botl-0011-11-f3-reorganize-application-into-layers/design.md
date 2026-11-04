# Design

## Context

Single package `es.upm.park` with: `App` (args, `--fly` directory creation/deletion, wiring of all stores and managers), `CommandRunner`, `CommandProcessor` (parsing + HELP + system date/price + cross-entity rules: ticket check on user delete, group locks, cascades), five managers, `UserValidator`, model classes, `Result`, and five `*FileStore` classes. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Physical separation with one-way dependencies: presentation → business → persistence → business.model.
- Identical behavior, output text and data files.

**Non-Goals:**
- New commands, data formats, interfaces for repositories, dependency injection, singletons (later F3 changes).

## Decisions

- **Packages and placement**:
  - `presentation`: `App` (argument parsing, input selection, exit status, shutdown hook), `CommandRunner`, `CommandProcessor` (splitting, keyword dispatch, usage messages, HELP/EXIT, parsing of date/price/field literals into `LocalDate`/`BigDecimal`/`UserField`).
  - `business`: `ParkController` (main controller: current date and price, user/group/ticket/attraction operations including the cross-entity rules previously in `CommandProcessor`; factory methods `open(dataDir)`, `open(dataDir, initialDate)`, `openTemporary(tmpRoot)`, and `close()`), `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `UserValidator`.
  - `business.model`: `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `Result`.
  - `persistence`: `UserFileStore`, `GroupFileStore`, `TicketFileStore`, `GroupTicketFileStore`, `AttractionFileStore`, and `DataDirectory` (temporary run directory creation and recursive deletion moved from `App`).
- **`ParkController` is the composition root.** It builds the stores for a data directory and the managers, so presentation never references persistence. `ParkController` implements `AutoCloseable`; `close()` deletes the temporary directory when it was opened with `openTemporary`, otherwise does nothing. `App` uses try-with-resources and the shutdown hook calls the same idempotent `close()`.
- **Why `Result` is in `business.model`**: it is the value returned by model operations (`Attraction.enter/exit`) as well as by managers and the controller; keeping it in the model avoids a model → business dependency. Presentation only prints it.
- **Capacity parsing** (`AttractionManager.parseCapacity`, used by `AttractionFileStore` on load) moves to `Attraction.parseCapacity` in the model so persistence does not depend on a business service.
- **Visibility**: members used across packages become `public` (store classes, manager methods already public); package-private test hooks in `App` stay in presentation with tests in the same package.
- **Tests** move to the package of the class under test; `CommandProcessorTest` builds the controller through `ParkController.open(dir, date)` and checks state through its accessors.
- **Behavioral equivalence check**: a scripted run of a fixed E1/E2 command file with the pre-refactor JAR (built from the parent commit) and the post-refactor JAR on copies of the same seed data; outputs and data files are compared byte by byte.
- **UML**: `uml/model.puml` regenerated with packages and the new `ParkController`/`DataDirectory`, following the notation rules of the `uml-model` spec.

## Risks / Trade-offs

- [Large mechanical move can silently change messages] → byte-for-byte output comparison against the previous JAR.
- [Package cycle business ↔ persistence] → avoided by placing the model in `business.model` and letting persistence depend only on it.
