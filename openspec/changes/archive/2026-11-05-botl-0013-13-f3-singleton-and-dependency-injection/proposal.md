# Proposal

## Why

Controllers (`ParkController` and the user, group, ticket and attraction managers) are plain classes that any code could instantiate several times, `ParkController` builds its own file stores and the invoice director is constructed inside an operation. The uniqueness of controllers must be explicit, while repositories must stay replaceable and be configured at startup and injected.

## What Changes

- `ParkController`, `UserManager`, `GroupManager`, `TicketManager` and `AttractionManager` apply the Singleton pattern: private constructor, one instance per execution obtained with `getInstance()`, and a static `configure(...)` that injects their dependencies at startup.
- Repository interfaces (`UserRepository`, `GroupRepository`, `TicketRepository`, `GroupTicketRepository`, `AttractionRepository`) are introduced in the persistence layer; the existing file stores implement them. Repositories are ordinary instances, not Singletons.
- A new startup component `ApplicationContext` (business layer) creates the file repositories for the normal data directory or the `--fly` run directory, injects them into the controllers, wires controller-to-controller collaborations (groups and tickets use the user manager, tickets use the group manager, the main controller uses all managers and the invoice director) and owns the `--fly` directory lifecycle.
- No business operation constructs a repository or a collaborating controller/service.
- Behavior, commands, data files and `--fly` behavior are unchanged; `uml/model.puml` updated.

The source suggests change ID `singleton-controllers-and-dependency-injection`; this batch uses the BOTL ID `botl-0013-13-f3-singleton-and-dependency-injection`. Mandatory milestone message: `[F3-C03] Add singleton controllers and dependency injection`.

## Capabilities

### New Capabilities
- `controller-lifecycle`: Unique controller instances, startup configuration and dependency injection of repositories and collaborators.

### Modified Capabilities
- None (observable command behavior is unchanged).

## Impact

- Managers and `ParkController`: singleton structure, `configure` methods, dependencies typed by repository interfaces.
- New `persistence.*Repository` interfaces; `business.ApplicationContext`; `presentation.App` uses `ApplicationContext`.
- Tests obtain controllers through `configure`/`getInstance`. `uml/model.puml` updated.
