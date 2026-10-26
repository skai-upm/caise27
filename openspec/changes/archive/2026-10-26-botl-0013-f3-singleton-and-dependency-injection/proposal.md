# Proposal

## Why

Each business controller represents a single management responsibility of the park, but nothing prevents several instances with diverging state. At the same time, repositories must stay replaceable (normal files, `--fly` files, other implementations), and collaborations between controllers should be configured once at start-up instead of being built inside operations.

## What Changes

- The business controllers (`UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController`) apply the Singleton pattern: private constructor and a single instance obtained with `getInstance()`.
- Repositories are not singletons. The business layer defines repository interfaces (`es.upm.parque.business.repository`); the persistence layer provides file-based implementations (`FileUserRepository`, …). Instances are created at start-up with the paths of the current mode and injected into the controllers.
- Each controller receives its repository and collaborating controllers through a `configure(...)` method called during application initialization (`App`), never constructing them inside operations.
- Normal and `--fly` modes differ only in the repository instances configured at start-up.
- `uml/model.puml` shows singletons, repository interfaces and implementations.

## Capabilities

### New Capabilities
- `component-wiring`: How controllers and repositories are instantiated and connected at start-up (Singleton controllers, injected repositories and collaborators).

### Modified Capabilities
- `application-architecture`: the business layer depends only on repository abstractions, not on persistence implementations.
- `uml-model`: the model shows singletons and repository abstractions.

## Impact

- Controllers: private constructors, `getInstance()`, `configure(...)`. Repository classes renamed to `File*Repository` implementing new interfaces. `App` becomes the explicit initialization point. Tests updated to obtain/configure the singletons.
- No change to commands, outputs or file formats.
