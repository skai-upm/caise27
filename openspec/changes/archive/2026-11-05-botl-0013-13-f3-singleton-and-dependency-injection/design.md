# Design

## Context

Business layer: `ParkController` (main controller; constructor + static `open`/`openTemporary` that create the five file stores and managers; `close()` deletes the `--fly` directory; creates `new InvoiceDirector()` inside invoice operations) and four managers with public constructors taking concrete `*FileStore` classes. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Explicit Singleton for the five controllers; repositories behind interfaces and injected; one startup composition root.

**Non-Goals:**
- A DI framework, global repository singletons, behavior changes.

## Decisions

- **Singleton form**: `private static final X INSTANCE = new X();`, private constructor, `public static X getInstance()`. Because collaborators are only known at startup, each singleton has `public static X configure(<dependencies>)` that injects them into the single instance, (re)loads its state from the injected repository and returns the instance. Reconfiguration replaces the state of the same instance (used by `--fly`, restarts in tests); it never creates a second instance. Eager initialization is thread-safe and simple; configuration happens once per run from the main thread.
- **Controllers in scope**: `ParkController`, `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager` — each owns one management responsibility for the whole execution. `UserValidator` is a stateless utility (no instance), builders are per-invoice objects (created per construction by design of the Builder pattern), `InvoiceDirector` is a stateless service injected into `ParkController`.
- **Repository abstractions** in `persistence`: `UserRepository` (`List<User> load()`, `save(List<User>)`), `GroupRepository` (`load(Function<String, Optional<User>>)`, `save`), `TicketRepository`, `GroupTicketRepository`, `AttractionRepository`; the `*FileStore` classes implement them. Interfaces stay in `persistence` so the existing dependency rule (business → persistence → model) holds.
- **`ApplicationContext`** (business): `start(Path dataDir, LocalDate date)` and `startTemporary(Path tmpRoot)` create the file repositories for the chosen directory and call, in dependency order, `UserManager.configure(userRepo)`, `GroupManager.configure(groupRepo, users)`, `TicketManager.configure(ticketRepo, groupTicketRepo, users, groups)`, `AttractionManager.configure(attractionRepo)`, `ParkController.configure(users, groups, tickets, attractions, new InvoiceDirector(), date)`. It implements `AutoCloseable` and deletes the temporary directory on `close()` (moved from `ParkController`). `App` uses it with try-with-resources and its shutdown hook.
- **Tests**: obtain controllers via `configure(...)` with file or in-memory repositories; a test asserts `getInstance()` identity across callers and that constructors are private (reflection).

## Risks / Trade-offs

- [Static singletons keep state across tests in the same JVM] → every test configures the singletons it uses in `@BeforeEach`; tests run sequentially (Surefire default).
- [Reconfiguration could be misused at runtime] → only `ApplicationContext` calls `configure` in production code; documented on the methods.
