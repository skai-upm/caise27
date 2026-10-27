# Proposal

## Why

Milestone F3-C03: controllers representing a unique management function must explicitly be Singletons, while repositories must remain replaceable dependencies created at application initialization and injected — so normal and `--fly` modes (or in-memory configurations) use differently configured repositories without changing business logic. Today only `ParkController` is a singleton, the other controllers are created ad hoc by the presentation layer, and repositories are created inside `ParkController`.

## What Changes

- Make `UserController`, `GroupController`, `TicketController`, `AttractionController` and `InvoiceController` explicit Singletons (private constructor, `getInstance()`), like `ParkController`.
- Remove repository creation from controllers: each controller receives the repositories it needs, and its collaborating controllers (e.g. `ParkController` for date/price and cross-entity rules) or services (the invoice builder), through a `configure(...)` injection method.
- Add an `ApplicationInitializer` (plain project code, no DI framework) that creates and configures the repositories for the execution mode — permanent files, `--fly` temporary directory, or in memory — and injects them into every controller; it also closes the execution (temporary directory clean-up).
- Repositories are not singletons: each execution/configuration gets new instances.
- No change to commands or outputs.
- Commit with the exact message `[F3-C03] Add singleton controllers and dependency injection`.

## Capabilities

### New Capabilities
- `dependency-injection`: Singleton controllers and initialization-time injection of repositories and collaborators.

### Modified Capabilities
<!-- none -->

## Impact

- `business.controller.*`, `business.invoice.InvoiceController`, new `business.controller.ApplicationInitializer`; `presentation.Main` and `CommandProcessor` obtain configured singletons; tests use the initializer instead of constructing controllers.
- Ambiguity flagged: repositories are injected as their concrete classes (which already support in-memory and file-backed configurations and can be subclassed, as the tests do); separate repository interfaces are not introduced because no alternative implementation is required by the source.
