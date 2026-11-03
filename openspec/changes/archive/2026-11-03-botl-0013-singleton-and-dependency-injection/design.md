# Design

## Context

Controllers (`business.controller`) are created only in `App.createProcessor(dataDir)` with constructor injection of concrete `*FileStorage` objects and of the controllers they collaborate with. `CommandProcessor` (presentation) receives `ParkController`. Tests create fresh controller graphs over temporary directories, sometimes twice in one test to simulate a restart. See proposal.md.

## Goals / Non-Goals

**Goals:** explicit Singleton for the unique controllers, dependency injection of replaceable repositories, wiring only at startup, testability.

**Non-Goals:** a DI framework, Singleton repositories, behaviour changes.

## Decisions

- **Which classes are unique:** the five controllers each own one application-wide responsibility (registry of users, groups, tickets, attractions; the park's date/price and cross rules). `CommandProcessor` and `CommandRunner` are presentation objects, not controllers. Repositories are deliberately not unique.
- **Singleton shape:** `private static volatile X instance`, private constructor, `public static synchronized X initialize(deps...)` and `public static X getInstance()` (throws `IllegalStateException` if `initialize` has not run). `initialize` is called by the composition root at the start of an execution; calling it again starts a new execution and replaces the instance. That is the test seam that lets one JVM run several executions (restart tests) without global repository state. Alternative — enum singletons or eager static instances — rejected because the instances need runtime dependencies (paths).
- **Repository interfaces** in `persistence`: `UserRepository { List<User> load(); void save(List<User>); }`, `GroupRepository { load(List<User>); save(...) }`, `TicketRepository`, `GroupTicketRepository`, `AttractionRepository` with the current signatures; the `*FileStorage` classes implement them. Controllers keep the field names (`storage`, `groupStorage`) with interface types.
- **Startup wiring (App):** create the five repositories for `dataDir` (`.` or the `--fly` directory), then `UserController.initialize(userRepo)`, `GroupController.initialize(groupRepo, users)`, `TicketController.initialize(ticketRepo, groupTicketRepo, users, groups)`, `AttractionController.initialize(attractionRepo)`, `ParkController.initialize(users, groups, tickets, attractions)`, then `new CommandProcessor(ParkController.getInstance())`.
- **Tests** call `initialize` instead of `new`; a new test checks identity (`users()` of the park, `getInstance()`), the not-initialised error and an in-memory repository.

## Risks / Trade-offs

- [Static state shared by tests in one JVM] → every test initialises its own graph first; surefire runs test classes sequentially.
- [Re-initialisation replaces instances] → only the composition root calls it; documented as the start of a new execution.
