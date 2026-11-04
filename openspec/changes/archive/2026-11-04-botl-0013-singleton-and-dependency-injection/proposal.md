# Proposal

## Why

`ParkController` — the single main controller that owns the system date and ticket price — can currently be instantiated any number of times, and it builds its own repositories and services in `ParkController.open(dataDir)`; it also creates its `InvoiceDirector` collaborator inside each invoice operation. Delivery 3 asks to make conceptually unique controllers explicit Singletons and to create/configure repositories and collaborators at startup and inject them.

## What Changes

- `ParkController` applies the Singleton pattern per execution: a private constructor, `initialize(...)` (once per execution, with its injected collaborators), `getInstance()` for every element that needs it, and `release()` when the execution ends. A second `initialize` while an instance exists is rejected.
- New composition root `com.parque.ApplicationContext`: at startup it creates and configures the five repositories with the paths of the chosen data directory (permanent or `--fly` temporary), creates the services injecting those repositories (and `UserService`/`GroupService` where needed), creates the `InvoiceDirector`, and initializes the `ParkController` singleton with all of them; `stop()` releases it.
- `ParkController.open(dataDir)` is removed: the controller no longer constructs repositories or services; `InvoiceDirector` is injected instead of being created inside each invoice operation (builders remain per-invoice products).
- `CommandProcessor` obtains the controller through `ParkController.getInstance()` (or receives it explicitly).
- Repositories are deliberately **not** Singletons: they are plain objects created per configuration, so normal and `--fly` runs use different paths with the same controller and service logic.
- No command, response, rule or data-format change.

## Capabilities

### New Capabilities
- `component-wiring`: Singleton main controller per execution, repositories created at startup and injected, collaborator wiring during initialization.

### Modified Capabilities
- `application-layering`: "Layered packages" also allows the composition root `ApplicationContext` directly in `com.parque`.

## Impact

- **Code**: `ParkController` (singleton API, injected `InvoiceDirector`, no `open`), new `ApplicationContext`, `App` (start/stop around each run), `CommandProcessor` (default constructor uses `getInstance()`, convenience constructors removed); tests use a helper that releases and initializes the singleton.
- **Docs**: `uml/model.puml` (`<<Singleton>>`, new `ApplicationContext`, injected `InvoiceDirector`).
- **Mandatory milestone message**: `[F3-C03] Add singleton controllers and dependency injection`.
- **Interpretation recorded**: the only controller class in the current code is `ParkController` (business layer); `UserService`, `GroupService`, `TicketService` and `AttractionService` are services — the source distinguishes "another controller or service" — so they are not turned into Singletons but are created once at startup and injected. "Unique per execution" is enforced by `initialize`/`release`, which also lets several executions run one after another in the same JVM (as the test suite does).
