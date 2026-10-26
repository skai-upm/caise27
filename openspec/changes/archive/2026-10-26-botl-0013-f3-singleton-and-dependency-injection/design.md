# Design

## Context

Controllers are created with `new` in `App.execute(...)`, receiving concrete repository classes from `es.upm.parque.persistence` and collaborating controllers through constructors. This creates a business → persistence dependency (noted in the layering change) and allows several controller instances.

## Goals / Non-Goals

**Goals:** one instance per controller; repositories chosen at start-up and injected; business free of persistence implementations; tests can still run each scenario on fresh data.

**Non-Goals:** a DI framework (only the Java standard library), changes to commands or file formats.

## Decisions

- **Singleton form**: eager `private static final X INSTANCE = new X();`, private constructor, `public static X getInstance()`. Thread-safe without synchronization. Alternative: lazy `synchronized getInstance` — unnecessary for a single-threaded CLI.
- **Injection by `configure(...)`** (method injection) instead of constructor injection, because a singleton's constructor runs once while each execution (normal, `--fly`, tests) needs its own repositories. `configure` stores the dependencies, reloads data from the repository and clears registered checks, then returns `this`. Signatures:
  - `UserController.configure(UserRepository)`
  - `GroupController.configure(GroupRepository, UserController)`
  - `TicketController.configure(TicketRepository, UserController, GroupController, SystemDate)` — also registers its user-deletion and group-modification checks on the injected controllers (so `UserController`/`GroupController` must be configured first).
  - `AttractionController.configure(AttractionRepository, TicketController, SystemDate)`
  - `InvoiceController.configure(UserController, GroupController, TicketController, SystemDate, InvoiceBuilder, InvoiceDirector)`
- **Repository abstractions** in `es.upm.parque.business.repository` (`UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, each with `load(...)`/`save(...)`). Persistence classes are renamed `FileUserRepository`, `FileGroupRepository`, `FileTicketRepository`, `FileAttractionRepository` and implement them. This removes the business → persistence dependency.
- **`App` is the initialization point**: creates `SystemDate` and the file repositories for the chosen data directory, configures the singletons in dependency order, then creates `CommandProcessor` with the configured controllers (constructor injection; the processor is not a business controller).
- **Tests** obtain controllers with `getInstance().configure(...)` on temporary directories; reconfiguration gives each test fresh state.
- **`ArchitectureTest`** additionally checks that no business source imports `es.upm.parque.persistence`, and a test checks each controller has no public constructor and `getInstance()` returns the same object.

## Risks / Trade-offs

- [Global mutable singletons can leak state between tests] → `configure` fully resets data and checks; every test configures its controllers in setup.
- [Using a controller before `configure`] → operations would fail with a null repository; `App` always configures before processing commands.
