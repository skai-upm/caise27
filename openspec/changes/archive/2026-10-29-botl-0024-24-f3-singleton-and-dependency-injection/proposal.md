# Proposal

## Why

Milestone F3-C03: controllers representing a unique management function must apply the Singleton pattern, while repositories must stay replaceable dependencies — created and configured during application initialization and injected — so normal and `--fly` persistence differ only in configuration, never in controller logic. Collaborations between controllers must also be set up by injection, using project code only (no DI frameworks).

## What Changes

- Apply Singleton (private constructor, single static instance, `getInstance()`) to the management controllers: `ParkController`, `UserService`, `GroupService`, `TicketService`, `AttractionService`, `InvoiceService`. They receive their collaborators through a `configure(...)` injection method instead of creating them.
- Repositories and storages are **not** singletons: a new composition root `business.ParkInitializer` creates repository and storage instances for the chosen configuration (in-memory, permanent directory, or temporary `--fly` directory) and injects them, together with controller-to-controller collaborations, into the singletons.
- `ParkController` keeps its public API; its initialization methods delegate wiring to `ParkInitializer`.
- Commit with the mandatory message `[F3-C03] Add singleton controllers and dependency injection`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `layered-architecture`: Singleton controllers, injected (non-singleton) repositories, and initialization-time wiring.

## Impact

- All services and `ParkController`; new `ParkInitializer` and `ParkDependencies`; tests obtain services through `getInstance().configure(...)`.
- Ambiguity flagged: repositories remain concrete classes; replaceability is provided by injection (any instance or subclass can be supplied), and the file-backed storages behind them are configured by path.
