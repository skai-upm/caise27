# Design

## Context

After the layering change:
- `App.run` builds the repositories (paths from the users file or the `--fly` directory), then the managers with those repositories injected, then `new ParkController(managers…)`, `CommandProcessor(controller)` and `CommandRunner`.
- `ParkController` has public constructors and creates `new InvoiceDirector()` inside `createUserInvoice`/`createGroupInvoice`.
- Tests create many controllers and managers with temporary files. `AppTest` calls `App.run` many times in one JVM.

## Goals / Non-Goals

**Goals:**
- Apply Singleton exactly where uniqueness is a domain/architecture fact, with an explicit lifecycle that fits several executions in one JVM (tests).
- Constructor injection everywhere else.

**Non-Goals:**
- A DI container or framework.
- Making managers or repositories global.

## Decisions

- **Which classes are unique?** The only class in `business.controller` is `ParkController`. It holds the run-wide system date and price and orchestrates all operations, so there must be exactly one per execution. The managers in `business.service` are services: each depends on the repositories it is given, and tests legitimately use several (for example a "restarted" manager over the same files). Making them Singletons would add global state without a domain reason and would hinder replaceability. Repositories are explicitly not Singletons, per the source.
- **Singleton shape for `ParkController`:**
  - `private static ParkController instance` and private constructors
  - `static synchronized ParkController initialize(UserManager, GroupManager, TicketManager, AttractionManager, InvoiceDirector[, LocalDate startupDate])`, which throws `IllegalStateException` if already initialised
  - `static ParkController getInstance()`, which throws `IllegalStateException` if not initialised
  - `static synchronized void shutdown()`, which releases the instance

  An eager `static final` instance is rejected because the controller needs collaborators that only exist after startup configuration (and `--fly` changes them). Lazy creation inside `getInstance()` is rejected for the same reason.
- **Lifecycle in `App`.** `runWith` calls `ParkController.initialize(...)` after building the services and wraps the rest of the run in `try/finally { ParkController.shutdown(); }`. Each execution therefore gets exactly one controller, and back-to-back runs in one JVM (tests, `--fly`) work.
- **Access.** `CommandProcessor` keeps receiving the controller through its constructor (injection), and `App` passes `ParkController.getInstance()`. Any other component can call `getInstance()` and obtains the same object.
- **Injected `InvoiceDirector`.** It is stateless and created once in `App`, then held in a `invoiceDirector` field of `ParkController`. Concrete invoice builders are still created per invoice inside the operation. That is inherent to the Builder pattern (one builder per product), and they are not services, repositories or controllers.
- **Tests.**
  - Tests that need a controller call `ParkController.shutdown()` before `initialize(...)`, and in `@AfterEach`, so that no instance leaks between tests.
  - A new `ParkControllerTest` covers same instance, double initialisation refused, `getInstance()` before initialisation fails, and a new instance after shutdown.
  - `AppTest` already runs normal and `--fly` executions back to back, which exercises the lifecycle and the repository substitution.
  - A repository test shows two `UserFileRepository` instances with different files coexisting.

## Risks / Trade-offs

- [Static state in tests] → explicit `shutdown()` in `@AfterEach`. Tests run sequentially, which is the Surefire default.
- [`shutdown()` could be misused mid-run] → it is called only by `App` at the end of an execution and by tests.
