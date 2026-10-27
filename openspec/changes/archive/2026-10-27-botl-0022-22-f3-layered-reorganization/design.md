# Design

## Context

All 29 production classes are in `es.upm.amusementpark`. `App` currently creates `UserFileStorage`/`GroupFileStorage`/… and repositories, and creates/deletes the `--fly` directory; `CommandProcessor` prints errors from many per-command `try/catch` blocks and returns `null` from parse helpers.

## Goals / Non-Goals

**Goals:**
- Layered packages, presentation free of persistence, exception-based error flow, identical behaviour.

**Non-Goals:**
- Dependency injection (F3-C03), new features.

## Decisions

- **Packages:**
  - `presentation`: `App`, `CommandRunner`, `CommandProcessor`, `InvalidCommandException`.
  - `business.model`: `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`.
  - `business.service`: `ParkController`, `UserService`, `GroupService`, `TicketService`, `AttractionService`, `UserValidator`.
  - `business.exception`: `ParkException` (base) and `User/Group/Ticket/AttractionOperationException`.
  - `persistence`: repositories, file storages, `TextFiles`, `PersistenceException`, `DataDirectories`.
- **Loading:** `ParkController.load(Path dataDir)` builds the repositories with their storages (business uses persistence; persistence uses business model — the usual layered dependency for this course; repository interfaces are not introduced). Skipped-line warnings are returned to `App` for printing.
- **`--fly`:** `persistence.DataDirectories.createTemporary(base)` / `delete(dir, base)`; `App` only decides when to call them.
- **Exceptions:** `ParkException extends Exception`; operation exceptions extend it. `PersistenceException extends RuntimeException` replaces `UncheckedIOException`. `InvalidCommandException` (presentation) is thrown by `requireElements`, `parseDate`, `parsePrice`, `parseCapacity`, `parseField`. `CommandProcessor.process` has a single `catch (ParkException | InvalidCommandException | PersistenceException)` printing `ERROR: <message>`, keeping the catch-all for unexpected runtime errors.
- **Visibility:** `Attraction.enter/exit` become public (used by `AttractionService` in another package); `App.run` stays package-private for tests in `presentation`.
- **Tests:** moved to the package of the class under test; a new `ArchitectureTest` checks packages and dependency rules; `UmlModelTest` stays in the root test package.
- **Mandatory commit:** `[F3-C01] Reorganize application into layered packages` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Large mechanical move] → Whole test suite plus a before/after comparison of the Delivery 2 session outputs and data files.
