# Design

## Context

See proposal.md – Why. `ParkController` (singleton) has `start(UserRepository, LocalDate)` (in-memory) and `start(Path, LocalDate)` which builds file stores and repositories itself, then `wire(...)` creates services with `new`. Services take repositories in constructors and are also created with `new` in unit tests.

## Goals / Non-Goals

**Goals:**
- Clear composition root; controllers as singletons; repositories as injected instances.

**Non-Goals:**
- Changing repository or store behavior.

## Decisions

- **Singleton controllers** (`business.service`): each service gets a private constructor, `private static final X INSTANCE`, `public static X getInstance()`, and `public X configure(...)` receiving its repositories/collaborators (setter-style injection is the only option for a singleton whose dependencies are chosen at runtime). Fields become non-final, assigned by `configure`.
- **`ParkRepositories`** (record in `persistence.repository`): `users`, `groups`, `attractions`, `tickets`. Factories `inMemory()`, `inMemory(UserRepository)` and `load(Path dataDir)` (file stores; load order users → groups → attractions → tickets). This is where repository configuration lives; it is not a singleton.
- **Composition root**: `Main` builds `ParkRepositories.load(data.path())` from the `DataDirectory` (normal or `--fly`) and calls `ParkController.getInstance().start(repositories, LocalDate.now())`. `start` injects: `UserService.configure(users)`, `TicketService.configure(tickets, users, this::getCurrentDate, this::getBasePrice)`, `GroupService.configure(groups, users, TicketService.getInstance()::hasActiveGroupTicket)`, `AttractionService.configure(attractions)`, `InvoiceService.configure(users, groups, tickets, new TextInvoiceBuilder())`.
- **Tests**: replace `new XService(...)` with `XService.getInstance().configure(...)`; persistence tests use `ParkRepositories.load(dir)`; a new test checks singleton identity/private constructors and that two configurations are honored.
- **UML**: `<<singleton>>` stereotypes on the controllers.

## Risks / Trade-offs

- [Singletons keep state between tests] → every test configures them in its set-up through `start` or `configure`.
