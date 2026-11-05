## Why

Delivery 3, milestone F3-C03 (source section 5), requires two things:
- Controllers that represent a unique management function during an execution apply the Singleton pattern explicitly.
- Repositories are treated as replaceable dependencies, created and configured during application initialization and injected where they are needed. Collaborations between controllers are also established at initialization, using hand-written Java code and no DI framework.

Today only `ParkController` is a singleton, and it creates its group, ticket and attraction repositories itself. The managers are instantiated by each presentation handler, and `AttractionManager` creates its own `TicketManager` locally.

## What Changes

- Each business manager becomes a Singleton with a private constructor and `getInstance()`:
  - `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `SystemManager` and `InvoiceManager`.
  - `ParkController` remains a Singleton.
- New `persistence.ParkRepositories`: a bundle of the four repositories and the optional park file store. It is built by factory methods for in-memory or file-backed configurations, and saving goes through it. Repositories stay ordinary, non-singleton classes.
- New `business.ParkConfiguration`: the composition root, written in plain Java.
  - `initialize(ParkRepositories)` injects the repositories into `ParkController`.
  - It then injects `ParkController` into every manager, and the `TicketManager` collaborator into `AttractionManager`.
- `ParkController.startExecution` receives a `ParkRepositories` instead of creating repositories.
- `ParkSession` builds the repositories for the selected storage location (normal or `--fly`) and calls `ParkConfiguration.initialize`. Controller logic is identical in both modes.
- Presentation handlers receive their manager in their constructor; `CommandProcessor` obtains the configured singletons. No handler or manager creates another manager or a repository.
- A manager used before initialization reports an `IllegalStateException`, with a clear message.
- Externally observable command behaviour, persistence formats and `--fly` semantics are unchanged.

## Capabilities

### New Capabilities
- `controller-configuration`: singleton controllers, repositories injected as replaceable dependencies, controller collaborations wired at initialization, and no DI framework.

### Modified Capabilities
<!-- none: command behaviour is unchanged -->

## Impact

- `business`: all managers, `ParkController` and `ParkSession`, plus the new `ParkConfiguration`.
- `persistence`: the new `ParkRepositories`.
- `presentation`: the command handler constructors and `CommandProcessor`.
- Tests switch from `ParkController.startExecution(users)` to `ParkConfiguration.initialize(ParkRepositories...)`.
- The UML model, the UML consistency test and README are updated.
