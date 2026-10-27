# Design

## Context

`ParkController` is already a singleton holding the repositories, date and price. Services are created with `new` (`CommandProcessor`, `App`, tests) and read `ParkController.getInstance()` and its repositories inside each operation. `ParkController.load` builds the file-based repositories itself.

## Goals / Non-Goals

**Goals:**
- Singleton controllers; repositories created by a factory and injected; no per-operation dependency lookups.

**Non-Goals:**
- Repository interfaces with alternative implementations (the same classes are configured with different storages/paths), DI frameworks.

## Decisions

- **Singleton services:** `private static final XService INSTANCE = new XService()`, private constructor, `getInstance()`. Each has `configure(...)` receiving exactly its collaborators:
  - `UserService.configure(UserRepository, GroupRepository, ParkController)`
  - `GroupService.configure(UserRepository, GroupRepository, ParkController)`
  - `TicketService.configure(UserRepository, GroupRepository, TicketRepository, ParkController)`
  - `AttractionService.configure(AttractionRepository, TicketRepository, ParkController)`
  - `InvoiceService.configure(UserRepository, GroupRepository, TicketRepository, ParkController)`
  The `ParkController` collaboration provides the system date/price and derived lock checks.
- **Composition at initialization:** `ParkController.initialize(users, groups, tickets, attractions, today)` stores the injected repositories and wires every controller with `configure(...)`. Shorter `initialize` overloads (used by tests) pass in-memory repositories.
- **Repository factory:** `persistence.ParkRepositories` (record of the four repositories plus ignored-line count) with `fromDirectory(Path)` (file storages in that directory) and `inMemory()`. `ParkController.load(dir, today)` = `initialize(ParkRepositories.fromDirectory(dir), today)`; `--fly` passes the temporary directory — same classes, different path.
- **Presentation:** `CommandProcessor(PrintStream)` takes the controller singletons once in its constructor. The "unexpected error" test now injects a failing repository instead of subclassing a service.
- **Mandatory commit:** `[F3-C03] Add singleton controllers and dependency injection` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Singletons keep state across tests in one JVM] → Every test initializes the controller (which re-wires all controllers) in its setup, as `App.run` does for every execution.
