# Proposal

## Why

Milestone F3-C03 requires controllers with a unique management responsibility to apply Singleton explicitly, and repositories to be replaceable dependencies created and configured during application initialization and injected into the elements that need them (so normal and `--fly` mode differ only in repository configuration). Today the services are freely instantiable, `Park.start` creates the repositories itself, and `CommandProcessor` creates some services locally.

## What Changes

- The controllers `UserService`, `GroupService`, `TicketService`, `AttractionService` and `InvoiceService` become explicit Singletons (private constructor, `getInstance()`), receiving their repositories and collaborators through an injection method.
- Repositories are NOT singletons: a `Repositories` set is created at start-up (file-backed for a data directory, or in-memory) and injected into `Park` and the controllers.
- A composition root (`presentation.ApplicationInitializer`) creates and wires everything during initialization and builds the `CommandProcessor` with the controllers injected; nothing creates its dependencies inside operations.
- Implemented with plain Java (no DI framework). Command behavior unchanged.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `layered-architecture`: adds the Singleton controllers and dependency-injection requirements.

## Impact

- Services, `Park`, `CommandProcessor`, `App`; new `Repositories` and `ApplicationInitializer`; tests rewired to use the initializer.
