# Proposal

## Why

Milestone F3-C03 requires controllers with a unique management responsibility to apply Singleton, and requires repositories to be replaceable dependencies. Repositories must be created and configured during application initialization and injected into the elements that use them, so that normal and `--fly` persistence differ only in configuration. Today `App` drives saving and loading directly through a `ParkStorage` that builds its own repositories internally, and central management has no persistence dependency it can be given.

## What Changes

- Keep `ParkController` as an explicit Singleton (the only controller with a unique execution-level responsibility). Repositories are not singletons.
- Add the business port `ParkRepository` (load the park state, save the park state), whose failures are reported as an unchecked `RepositoryException`. The business layer still doesn't depend on persistence.
- `persistence.ParkStorage` implements the port and receives its `AttractionRepository`, `GroupRepository` and `TicketRepository` through its constructor. The initialization factory `ParkStorage.inDirectory(path)` builds and configures them for a data directory.
- Central management gets the repository by injection (`setRepository`) and exposes `loadState()` / `saveState()`, so it never creates repositories itself.
- `App`, the composition root, creates the storage for the chosen mode (normal `data/` or `--fly` temporary directory), injects it into central management, loads the state, and configures saving after each command through central management.
- Collaborations, such as user lookup, keep being injected at initialization through setters, never created inside operations. No DI framework is used.

### Out of scope
- New persistence implementations, and behavior changes.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `layered-architecture`: adds Singleton controllers and injected, replaceable repositories.

## Impact

- New `business.ParkRepository` and `business.RepositoryException`.
- `persistence.ParkStorage` (constructor injection, factory).
- `ParkController` (repository dependency).
- `App` wiring.
- Tests (`DependencyInjectionTest`, persistence tests use the factory) and the UML model.
