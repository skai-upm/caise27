## Context

- `ParkController` is a holder-idiom singleton. `startExecution(UserRepository[, ParkFileStore])` creates the group, ticket and attraction repositories itself and keeps the store for saving.
- `ParkSession.open` (business) chooses the `StorageLocation` and builds `UserRepository` and `ParkFileStore`.
- Presentation handlers construct their manager with `new XManager(park)`. `AttractionManager` constructs a `TicketManager`.
- Tests start executions through `TestSupport.processor(users)` and direct `startExecution` calls.

## Goals / Non-Goals

**Goals:**
- Explicit singletons for controllers.
- Repositories built and injected only at initialization.
- Collaborations wired in one composition root, with no framework.

**Non-Goals:**
- Changing command behaviour or file formats.
- Making `CommandProcessor` a singleton, since it is presentation and not a controller.
- Making repositories or stores singletons.

## Decisions

1. **`ParkRepositories` (persistence).**
   - An immutable bundle with getters for the four repositories, plus `save()`, which writes groups, tickets and attractions when a `ParkFileStore` is configured.
   - Factories:
     - `inMemory()`;
     - `inMemory(UserRepository users)`;
     - `fileBacked(UserRepository users, ParkFileStore store)`, which creates empty repositories and calls `store.load(...)`.
   - `getMalformedLineCount()` delegates to the store.
   - The bundle is created by the initialization code, never by controllers.
   - Alternative considered: passing four repositories and a store as separate parameters. It was rejected because the bundle keeps the injection signature stable as repositories are added in later milestones (passes, attraction types).
2. **`ParkController.startExecution(ParkRepositories)`.**
   - Stores the injected bundle and resets the date and price.
   - `persist()` delegates to `repositories.save()`.
   - The getters return the injected repositories.
   - The old overloads that created repositories are removed.
3. **Manager singletons.**
   - Holder idiom with a private constructor.
   - Dependencies are injected by a public `inject(...)` method: `inject(ParkController)`, or for `AttractionManager`, `inject(ParkController, TicketManager)`.
   - Fields are accessed through a private `park()` accessor that throws `IllegalStateException("the application has not been initialized")` while the field is null.
   - The managers have no other state, so re-initialization between executions or tests is safe.
4. **`ParkConfiguration.initialize(ParkRepositories)` (business).** This is the single composition root. It:
   - starts the `ParkController` execution with the repositories;
   - injects the controller into each manager;
   - injects `TicketManager` into `AttractionManager`.

   `ParkSession.open` builds `ParkRepositories.fileBacked(new UserRepository(new UserFileStore(dir/users.txt)), new ParkFileStore(dir))` for the directory chosen by `StorageLocation`, then calls `initialize`.
5. **Presentation.**
   - `UserCommands(UserManager)`, `GroupCommands(GroupManager)`, `TicketCommands(TicketManager)`, `AttractionCommands(AttractionManager)` and `InvoiceCommands(InvoiceManager)` receive their manager.
   - `CommandProcessor()` passes `X.getInstance()` to each handler and uses `SystemManager.getInstance()`.
   - Presentation still never imports persistence; tests do that through `TestSupport`.
6. **Tests.**
   - `TestSupport.processor(UserRepository)` becomes `ParkConfiguration.initialize(ParkRepositories.inMemory(users))` followed by `new CommandProcessor()`. Other direct calls are migrated the same way.
   - A new `ControllerConfigurationTest` checks:
     - the singletons and their constructor visibility;
     - that repositories are not singletons;
     - injection with two configurations;
     - that no `new XManager(` or `new XRepository(` appears in business sources other than `ParkConfiguration` and `ParkSession` (the initialization code);
     - the uninitialized-use error;
     - that `pom.xml` has no DI framework.

## Risks / Trade-offs

- Mutable injection into singletons means a test that forgets to initialize gets an `IllegalStateException`. That is acceptable, and the message is clear.
- Singletons keep their references across executions in the same JVM. `initialize` re-injects every time, so the state always comes from the latest configuration.
