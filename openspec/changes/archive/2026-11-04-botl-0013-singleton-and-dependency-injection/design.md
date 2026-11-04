# Design

## Context

`ParkController` (business.controller) has public constructors and a static `open(dataDir)` that builds repositories and services; `createUserInvoice/createGroupInvoice` do `new InvoiceDirector()`. `App` calls `ParkController.open(dataDir)` and `new CommandProcessor(controller)`. Tests build controllers through `CommandProcessor` convenience constructors or `ParkController.open`. `App.run` is executed many times within one JVM by tests.

## Goals / Non-Goals

**Goals:** explicit Singleton for the main controller per execution; one composition root that creates and injects everything; no repository/collaborator construction inside operations.

**Non-Goals:** DI framework; Singleton repositories or services; repository interfaces.

## Decisions

- **Singleton API** (`ParkController`):
  - `private ParkController(UserService, GroupService, TicketService, AttractionService, InvoiceDirector, LocalDate)`.
  - `public static synchronized ParkController initialize(users, groups, tickets, attractions, invoiceDirector)` (starts at `LocalDate.now()`) and an overload with an explicit initial date; throws `IllegalStateException` if an instance exists.
  - `public static synchronized ParkController getInstance()` — `IllegalStateException` if not initialized.
  - `public static synchronized void release()` — ends the execution's instance (idempotent).
  - Alternative: eager `static final INSTANCE` — rejected because its collaborators depend on the data directory chosen at runtime (`--fly`) and the date must restart per execution.
- **Composition root** `com.parque.ApplicationContext`:
  - `static ParkController start(Path dataDir)`: `new UserRepository(dataDir.resolve("users.txt"))`, … → `new UserService(userRepo)`, `new GroupService(groupRepo, users)`, `new TicketService(ticketRepo, groupTicketRepo, users, groups)`, `new AttractionService(attractionRepo)`, `new InvoiceDirector()` → `ParkController.initialize(...)`.
  - `static void stop()` → `ParkController.release()`.
  - File-name constants move here from `ParkController`.
- **App**: `runWithDataDir` calls `ApplicationContext.start(dataDir)` inside `try`, builds `new CommandRunner(new CommandProcessor(), out, err)`, and calls `ApplicationContext.stop()` in `finally`, so each run is one execution.
- **CommandProcessor**: `CommandProcessor()` uses `ParkController.getInstance()`; `CommandProcessor(ParkController)` kept for explicit injection. The service-based convenience constructors are removed (they would create controllers outside the singleton).
- **Tests**: a test helper `com.parque.TestPark` (test sources) offers `start(dir)` (= `ApplicationContext.stop(); ApplicationContext.start(dir)`) and `controller(users, groups, tickets, attractions[, date])` (= `release` + `initialize` with a new `InvoiceDirector`). A new `ComponentWiringTest` checks the singleton rules, that the constructor is private, that `ParkController` has no `open` method and no static factory touching persistence, and that two `ApplicationContext` starts on different directories write to their own directories.
- **Layering**: `LayeringTest` allows `ApplicationContext` in the root package and now forbids `business.controller` from depending on `persistence`.

## Risks / Trade-offs

- [Global state in tests] → every test that needs the controller (re)initializes it through `TestPark`; `App.run` always releases in `finally`.
- [Concurrent executions in one JVM] → not supported by design (one execution per process in production); `synchronized` accessors avoid races on initialization.
