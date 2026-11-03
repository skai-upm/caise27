# Proposal

## Why

Controllers are conceptually unique per execution but nothing enforces it, and they depend on concrete file-storage classes. The design must guarantee single controller instances and keep repositories replaceable (different implementations or paths, e.g. `--fly`), with all collaborations configured at startup.

## What Changes

- `UserController`, `GroupController`, `TicketController`, `AttractionController` and `ParkController` apply the Singleton pattern: private constructor, `initialize(...)` called once by the composition root with the injected dependencies, and `getInstance()` for every other access.
- Repository interfaces `UserRepository`, `GroupRepository`, `TicketRepository`, `GroupTicketRepository`, `AttractionRepository` (persistence layer), implemented by the existing `*FileStorage` classes; controllers depend only on the interfaces.
- `App` creates and configures the repositories (normal or `--fly` paths) and the controllers in dependency order at startup and injects them; no business operation constructs a repository or a controller.
- Repositories are not Singletons. No DI framework. Behaviour, commands and files unchanged.

## Capabilities

### New Capabilities
- `component-lifecycle`: unique controllers, replaceable injected repositories and startup wiring.

### Modified Capabilities
- `domain-model-diagram`: the diagram also shows the repository interfaces and the Singleton controllers.

## Impact

- Controllers (constructors/factories), new repository interfaces, `*FileStorage` (implements), `App.createProcessor`, tests (use `initialize`), `uml/model.puml`.
