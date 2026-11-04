# Proposal

## Why

The main controller must be unique per execution, but today nothing stops several `ParkController` instances from being created. Collaborations are mostly wired in `App`, yet `ParkController` still builds its `InvoiceDirector` inside each invoice operation. Repositories must stay replaceable (normal vs. `--fly` paths, other implementations later), so uniqueness has to be applied only where it is conceptually right, and the rest has to be injected at startup.

## What Changes

- `ParkController`, the single controller of the application, is the only class responsible for park management. It explicitly enforces one instance per execution with the Singleton pattern: `initialize(...)` once at startup, `getInstance()` everywhere else, and `shutdown()` at the end of the execution. Its constructor becomes private.
- The managers (`business.service`) and the repositories (`persistence`) are **not** Singletons. They are created and configured once at startup in `App` and injected through constructors. Repositories receive their file paths from `App`, so normal and `--fly` runs differ only in configuration.
- `InvoiceDirector` is created at startup and injected into `ParkController`, instead of being built inside each invoice operation.
- No external DI framework. Command behaviour, output and data files are unchanged.

## Capabilities

### New Capabilities
- `component-wiring`: Singleton main controller, startup creation and injection of services and repositories, replaceable repositories, unchanged behaviour in normal and `--fly` runs.

### Modified Capabilities
_None._

## Impact

- `ParkController` (Singleton API, injected `InvoiceDirector`), `App` (initialise/shutdown around each run), and the tests that create controllers.
- UML: `ParkController` marked as Singleton, with its `instance` self-reference and the `invoiceDirector` association.
- Delivery milestone message: `[F3-C03] Add singleton controllers and dependency injection`.
