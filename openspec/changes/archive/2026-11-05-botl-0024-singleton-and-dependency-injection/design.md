# Design

## Context

`Park` (singleton) creates repositories in `start(...)`; services are created with `new` in `App`, tests and `CommandProcessor` (which builds an `InvoiceService` and default services itself).

## Goals / Non-Goals

**Goals:** explicit Singleton controllers; repositories built once at initialization and injected; one composition root. **Non-Goals:** framework-like container, behavior changes.

## Decisions

- **Controllers as Singletons**: private constructor, static holder `getInstance()`, and `inject(...)` (setter injection, returns `this`) taking the repositories/collaborators. Setter injection is required because a singleton exists before the configuration is known (and `--fly` reconfigures it). Repositories remain normal classes.
- **`persistence.repository.Repositories`** (record of users/groups/tickets/attractions) with `files(Path dataDirectory)` (loads in dependency order, previously in `Park.start(Path)`) and `inMemory(...)` factories. `Park.start(Repositories)` only receives them; the old repository-creating `Park.start` overloads are removed.
- **Composition root `presentation.ApplicationInitializer.initialize(Repositories)`**: starts `Park`, injects repositories into each controller, wires collaborations (e.g. the invoice controller with the user/group/ticket repositories), and returns a `CommandProcessor` constructed with the controllers. `App` calls it with `Repositories.files(dataDir)` (normal or `--fly` directory).
- **CommandProcessor** receives all controllers through its constructor; convenience constructors that derived services are removed.
- Tests obtain processors via the initializer with in-memory repositories, keeping references to the repositories they inject.

## Risks / Trade-offs

- [Singleton state shared between tests] → each test re-initializes through the composition root.
