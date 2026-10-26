# Design

## Context

After botl-0012: managers take storages in constructors; `ParkController.load(DataDirectory)` builds everything; `ParkController.createUserInvoice` does `new InvoiceDirector()`. Tests instantiate managers freely. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One instance per controller, collaborators injected once at startup, repositories swappable.

**Non-Goals:**
- DI frameworks (standard library only), making repositories or model objects singletons, changing behavior.

## Decisions

- **Eager Singleton**: `private static final X INSTANCE = new X(); private X() {}; public static X getInstance()`. Thread-safe by class initialisation; the app is single-threaded anyway.
- **Method injection** `configure(...)` returning `this` (fluent): `UserManager.configure(UserRepository)`, `GroupManager.configure(UserManager, GroupRepository)`, `TicketManager.configure(UserManager, GroupManager, TicketRepository)`, `AttractionManager.configure(AttractionRepository)`, `ParkController.configure(UserManager, GroupManager, TicketManager, AttractionManager, InvoiceDirector, LocalDate initialDate)`. Configuring (re)loads the manager state from its repository and resets per-execution variables (date, price) — this also gives tests a clean state. Constructor injection is impossible with a parameterless singleton constructor; a mutable static "set instance" was rejected (breaks uniqueness).
- **Repository abstractions** in `persistence`: `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` with the current `load`/`save` signatures; `UserStorage` etc. implement them. Controllers' fields are typed by the interfaces.
- **Composition root** in `App.configure(DataDirectory)` (startup, presentation layer): creates file repositories from the data directory, the `InvoiceDirector`, and configures the singletons in dependency order (users → groups → tickets → attractions → park). `CommandProcessor` still receives only the `ParkController`.
- **Unconfigured use** throws `IllegalStateException("<X> is not configured")` via a private accessor, making ordering mistakes explicit.

## Risks / Trade-offs

- [Global state across tests] → every test (re)configures the singletons in `@BeforeEach`; JUnit runs tests sequentially in this project.
