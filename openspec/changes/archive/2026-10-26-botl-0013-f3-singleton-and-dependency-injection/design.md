# Design

## Context

`App.createProcessor(dataDir, mainController)` creates repositories and controllers with `new` and passes dependencies through constructors; `TestPark` duplicates this for tests; `UserControllerTest` and `MainControllerTest` construct controllers directly. Controllers load their data in the constructor.

## Goals / Non-Goals

**Goals:**
- One instance per controller class, created lazily and thread-safely.
- Initialisation is explicit and centralised: create repositories → configure controllers in dependency order → create the presentation component.
- Tests reuse the same initialisation path, re-configuring the singletons over a fresh temporary directory per test.

**Non-Goals:**
- A DI framework (none is allowed; plain Java), making `CommandProcessor` a singleton (presentation component; it receives the controllers by constructor injection).

## Decisions

- **Singleton form**: private constructor + initialization-on-demand holder (`private static final class Holder { static final X INSTANCE = new X(); }`, `public static X getInstance()`), lazy and thread-safe without synchronisation.
- **Injection by configuration method**: each controller exposes `configure(...)` taking its repository and collaborators (e.g. `TicketController.configure(TicketRepository, UserController, GroupController, MainController)`); it stores them and (re)loads its data from the repository. Re-configuring replaces repositories and state, which is how tests and the two execution modes select their data. Constructor injection is impossible with a singleton; setter-per-dependency would allow half-configured objects, so one method receives everything at once.
- **Guard**: controllers keep a `repository == null` check (`requireConfigured()`) at the start of public operations and throw `IllegalStateException("<Controller> is not configured")`.
- **`MainController.configure(Clock)`** sets the start date from the clock and resets the ticket price to the default — the per-execution state.
- **Composition root**: `App.configure(Path dataDir, Clock clock)` creates the five repositories, configures the controllers in dependency order (main, users, groups, tickets, attractions, invoices) and returns a `CommandProcessor` built with the controller instances. `TestPark` calls the same method and then exposes `getInstance()` of each controller.
- **UML**: controllers show a static `instance` association to themselves is not drawn (no field holds it directly — the holder class does); the model keeps the existing injected associations, which now come from `configure`.

## Risks / Trade-offs

- [Global mutable state shared by tests] → Tests run sequentially (JUnit default) and every test re-configures the singletons over its own temporary directory before use.
