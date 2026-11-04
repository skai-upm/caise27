# Design

## Context

`ParkSession.build(DataFiles)` already acts as a composition root: it constructs the storages and managers with `new` and passes them on. Tests construct managers directly in many places. `ParkController.createUserInvoice/createGroupInvoice` do `new InvoiceDirector()`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- An explicit, enforced Singleton per controller, with initialization order and dependencies visible in one place.

**Non-Goals:**
- Singleton repositories, a service locator or DI framework, lazy self-construction (the controllers need injected dependencies, so they cannot build themselves).

## Decisions

- **Singleton form (per controller):**
  ```java
  private static X instance;
  private X(deps) { ... }
  public static synchronized X initialize(deps) // throws IllegalStateException if instance != null
  public static synchronized X getInstance()   // throws IllegalStateException if instance == null
  public static synchronized void reset()       // instance = null
  ```
  Eager `getInstance()` self-construction was rejected: it would have to read paths or construct repositories itself, contradicting injection.
- **Execution lifetime = `ParkSession`:**
  - `build` initializes, in dependency order, `UserManager(userStorage)`, `GroupManager(groupStorage, UserManager)`, `TicketManager(ticketStorage, groupTicketStorage, UserManager, GroupManager)`, `AttractionManager(attractionStorage)` and `ParkController(managers, new InvoiceDirector())`.
  - `close()` calls `ParkSession.resetControllers()`. If building fails part-way, already-initialized controllers are reset.
  - A second session opened while one is active fails, because the controllers are already initialized.
- **Repositories:** still plain objects created in `ParkSession` from `DataFiles` (permanent or `--fly` directory), so different implementations or paths can be injected without touching controller code.
- **`InvoiceDirector`:** a stateless service, injected into `ParkController` and kept in a `director` field. Builders stay per-invoice objects because they hold the state of the invoice being built.
- **Tests:** every test class that creates controllers calls `ParkSession.resetControllers()` in `@BeforeEach`/`@AfterEach`. Restart simulations call `reset()` on the affected controller before re-initializing. A `ComponentWiringTest` covers the Singleton contract and `ParkSession` wiring.

## Risks / Trade-offs

- [Global state complicates tests] → Explicit `reset()` hooks per test, and session-scoped lifetime in production.
- [Controllers referenced through constructor injection *and* available globally] → Injection is the primary path; `getInstance()` exists to satisfy the Singleton access-point requirement.
