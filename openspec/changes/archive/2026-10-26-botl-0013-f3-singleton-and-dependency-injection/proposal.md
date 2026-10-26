# Proposal

## Why

Controllers are currently plain objects that anybody can instantiate (twice), and `ParkController.load` both builds repositories and wires controllers, while `ParkController` creates its `InvoiceDirector` inside each invoice operation. Delivery 3 asks to make conceptually unique controllers single-instance and to configure every collaboration by injection at startup, so repository implementations or locations can change without touching controller logic.

## What Changes

- `ParkController`, `UserManager`, `GroupManager`, `TicketManager` and `AttractionManager` apply the Singleton pattern (private constructor, `getInstance()`), and receive their collaborators through a `configure(...)` injection method called during initialization.
- Repository abstractions `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` (persistence); the existing file storages implement them and are NOT singletons. Controllers depend only on the abstractions.
- Startup (`App`) creates the repositories for the selected `DataDirectory` (normal `data/` or `--fly` temporary directory), creates the `InvoiceDirector` service, and injects everything in dependency order; `ParkController.load` is removed.
- `ParkController` uses the injected `InvoiceDirector` instead of constructing one per operation.
- No change to commands, responses or file formats.
- Required commit title: `[F3-C03] Add singleton controllers and dependency injection`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `application-architecture`: adds single-instance controllers and startup dependency injection of repositories and collaborators.

## Impact

- Controllers' constructors become private; tests obtain instances with `getInstance().configure(...)`.
- New repository interfaces; storages declare `implements`; UML updated (singleton self-associations, interface realisations).
