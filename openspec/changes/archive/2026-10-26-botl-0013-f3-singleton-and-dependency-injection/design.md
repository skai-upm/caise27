# Design

## Context

Controllers in `business.controller` receive repositories and collaborators via constructors, all called from `App.buildProcessor(Path)`. Tests also construct controllers directly, and simulate restarts by building new controllers over the same files. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Singleton controllers with setter-style injection at startup.
- Repositories remain plain objects chosen per run.

**Non-Goals:**
- A DI framework; making `CommandProcessor`, `SystemDate` or repositories singletons.

## Decisions

- **Eager singleton**: `private static final XController INSTANCE = new XController();`, private constructor, `public static XController getInstance()`. Eager creation is thread-safe without synchronization and the instances are cheap (no I/O in the constructor).
- **Injection via `configure(...)`**: each controller has `public XController configure(<dependencies>)` that stores the dependencies and (re)loads its data from the injected repository, returning `this`. Called once per run by `App`; reconfiguring is allowed, which lets tests and `--fly` use other repositories without new instances. Alternative: constructor injection into a singleton — impossible with a private no-arg constructor shared by all callers; a static holder of a lazily built instance with parameters — rejected, first caller would silently fix the dependencies.
- **Guard**: each public operation calls `requireConfigured()` throwing `IllegalStateException("<X>Controller is not configured")` if `configure` has not run.
- **Order** (in `App.buildProcessor`): users → groups(users) → tickets(users, groups, date) → attractions(tickets, date) → invoices(users, groups, tickets, builder). Ticket price resets to 40.00 on `configure` (it is per execution).
- **Tests**: `new XController(args)` becomes `XController.getInstance().configure(args)`; a "restart" reconfigures all controllers in dependency order so object identities stay consistent.
- **UML**: controllers are stereotyped `<<Singleton>>`; the static `INSTANCE` field is not drawn, since static constants are omitted from the model by convention.

## Risks / Trade-offs

- [Global state shared between tests] → each test reconfigures every controller it uses before acting; surefire runs test classes sequentially.
