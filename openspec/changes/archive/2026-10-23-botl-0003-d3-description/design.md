# Design

## Context

Delivery 2 code: `es.upm.park.{model, validation, persistence, service, cli}` plus `App`. `service.Park` is a singleton that both holds state and creates its own repositories in `open(DataPaths)`; `GroupService`/`TicketService`/`AttractionService` are created with `new` inside `CommandProcessor`; errors use one `OperationException`; passwords are plain text; `Attraction` is a single concrete class. See proposal.md for motivation and the specs for behaviour.

## Goals / Non-Goals

**Goals:**
- Layered packages with one-way intent: presentation → business → persistence interfaces; `App` is the composition root that wires everything.
- Singletons only for unique controllers; repositories are interfaces, created in `App` and injected.
- Generics with real effect on what each attraction type can admit.

**Non-Goals:**
- Reproducing the ten mandatory `[F3-C01]..[F3-C10]` commits (BOTL: one commit per proposal; same deviation as Deliveries 1–2).
- Migrating plain-text user files (explicitly not required); a legacy line simply never authenticates.

## Decisions

### Packages
- `es.upm.park.App` — composition root: parses arguments, builds `DataPaths`, `FileRepositories`, the `BCryptPasswordHasher`, configures singletons, builds the presentation objects, runs, cleans `--fly`.
- `presentation` — `CommandProcessor` (registry of `CommandDefinition`s, variants with different field counts), `CommandRunner`, `CommandResult`, `ValueParser`, `InvoiceFormatter`; `presentation.help` — `HelpGenerator` (receives a `HelpFormatter` by constructor), `HelpFormatter` interface, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `CommandHelp` record.
- `business.model` — `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `GroupDiscount`; `business.model.access` — `GeneralAccess`, `IndividualAccess`, `GroupAccess`; `business.model.attraction` — `Attraction<T extends GeneralAccess>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, `EntryResult`; `business.model.pass` — `Pass`, `SpeedPass`, `SpecialPass`, `PassRegistry`.
- `business.controller` — `Park` (central state, singleton), `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController` (all singletons).
- `business.invoice` — `Invoice`, `InvoiceSection`, `InvoiceItem`, `InvoiceBuilder` (interface), `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`.
- `business.exception` — `ParkException` (checked base) and `ValidationException`, `NotFoundException`, `ConflictException`, `BusinessRuleException`, `AuthenticationException`, `AccessDeniedException`.
- `business.validation.UserValidator`; `business.security` — `PasswordHasher`, `BCryptPasswordHasher`.
- `persistence` — repository interfaces `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, `PassRepository`, record `Repositories`, `DataPaths`, unchecked `PersistenceException`; `persistence.file` — `TextFiles`, `*FileRepository`, `FileRepositories.create(DataPaths)`.

### Singletons with injected dependencies
Each controller has a private constructor, `getInstance()` (static holder) and a `configure(...)` method that receives its collaborators; using a controller before `configure` throws `IllegalStateException`. Wiring in `App.initialize(DataPaths, PasswordHasher)`:
`Park.configure(repositories)` → `Park.load()`; `UserController.configure(park, hasher)`; `PassController.configure(park)`; `TicketController.configure(park, passController)`; `GroupController.configure(park)`; `AttractionController.configure(park, passController)`; `InvoiceController.configure(park, passController)`. Repositories are never singletons — `--fly` only passes a different `DataPaths` to `FileRepositories.create`. Tests inject the same file repositories on temp directories (and could inject in-memory ones). Alternative considered: constructor injection without singletons — rejected because the assignment requires singletons for unique controllers.

`Park` keeps the central state (date, price, users, groups, top-level tickets, attractions, passes) and the queries from Delivery 2 (`findTicket`, `isLocked`, `hasActiveTicket`, …) and implements `PassRegistry` lookups via `PassController`. Every successful mutating operation calls `Park.save()`.

### Exceptions
Business code throws `ParkException` subclasses chosen by meaning (not found / conflict / rule / validation / authentication / access). `CommandProcessor` is the single handling point: it converts any `ParkException` into `ERROR: <message>` and also catches `PersistenceException`/runtime exceptions so a command never stops processing. `App` handles start-up `PersistenceException`.

### Passwords
`PasswordHasher { String hash(String plain); boolean matches(String plain, String hash); }`. `BCryptPasswordHasher(logRounds)` uses jBCrypt; `matches` returns false (never throws) for non-BCrypt stored values. `App` uses 10 rounds; tests use 4 for speed. `User.password` becomes `User.passwordHash`; validation runs on the plain value before hashing; `USER UPDATE ... PASSWORD` stores a fresh hash.

### Ticket access capabilities and generic attractions
- `GeneralAccess` — `getId()`, `isValidOn(date)`, `getHolders()`, `getAdmittedTickets()` (individual tickets that enter).
- `IndividualAccess extends GeneralAccess` — `getUser()`; implemented by `IndividualTicket`.
- `GroupAccess extends GeneralAccess` — `getComposition()`; implemented by `GroupTicket`.
- `Attraction<T extends GeneralAccess>` holds `Class<T> accessType`; `enter(T ticket, boolean speed, PassRegistry passes)` is a template method: capacity check → `passesToConsume(T, speed, passes)` (type hook, validates only) → commit (consume passes, add occupants). Subclasses: `GeneralAttraction extends Attraction<GeneralAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>` (SpeedPass check iterates `getComposition()`), `SpecialAttraction extends Attraction<IndividualAccess>` (rejects SPEED, requires the ticket's unconsumed Special Pass). `AttractionController` checks `attraction.accepts(ticket)` (`accessType.isInstance`) and calls a private generic helper `<T extends GeneralAccess> EntryResult admit(Attraction<T> a, GeneralAccess t, ...)` that casts with `a.getAccessType().cast(t)`. INDIVIDUAL vs SPECIAL differ only in attraction behaviour, not in the ticket hierarchy. `AttractionType` enum is a factory (`create(id, name, capacity)`) and is persisted.

### Passes
`Pass` (abstract: `id`, `IndividualTicket ticket`, `BigDecimal price`, `boolean consumed`, `consume()`); `SpeedPass` (price = 20% of `ticket.getPrice()`, HALF_UP to cents), `SpecialPass` (price = `ticket.getPrice()`). Prices are stored, so later price changes do not affect them. Passes reference their ticket; tickets do not reference passes (lookups go through `PassController`, which implements `PassRegistry`). One identifier space for all passes, separate from tickets. A Special Pass can be bought again after the previous one was refunded (a refund removes the purchase; consumption does not) — documented assumption. `PassController.deleteAllFor(IndividualTicket)` is used by `TicketController` for the cascades; all checks run before any removal.

### Invoices (Builder)
`InvoiceBuilder` (common abstraction: `title(...)`, `recipient(...)`, `addItem(sectionTitle, item)`, `build()`); `AbstractInvoiceBuilder` collects sections in insertion order, skips sections with no items, and computes the total from items only. `UserInvoiceBuilder` adds `forUser(User)`, `addIndividualTicket`, `addSpeedPass`, `addSpecialPass`; `GroupInvoiceBuilder` adds `forGroup(Group)`, `addGroupTicket(GroupTicket)` whose composition becomes non-charged detail lines of the item. `InvoiceController` is the director. `Invoice` is immutable; `InvoiceFormatter` (presentation) renders it. Invoices are not persisted.

### Help
`CommandProcessor` exposes its definitions as `List<CommandHelp(syntax, description)>`. `HELP` and `HELP EXTENDED` handlers call two `HelpGenerator`s created in `App` with `SyntaxHelpFormatter` and `ExtendedHelpFormatter` respectively and injected into `CommandProcessor`. New formats only need a new `HelpFormatter`.

### Persistence formats
- `attractions.txt`: `id;name;capacity;type` (missing type → GENERAL).
- `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;consumed(true|false)`; tickets resolved through `Park.findTicket` (standalone or component); unresolved lines skipped with a warning.
- Other files unchanged; `users.txt` field 5 now holds the hash.

### Packaging
`org.mindrot:jbcrypt:0.4` (compile scope). `maven-assembly-plugin` 3.7.1 `jar-with-dependencies` with `appendAssemblyId=false` bound to `package` replaces `target/amusement-park.jar` with a self-contained executable JAR (Main-Class `es.upm.park.App`).

## Risks / Trade-offs

- [Big-bang package move breaks history readability] → One commit per proposal is imposed by BOTL; tests move with the code and are re-run in full.
- [Singleton state leaks between tests] → `App.initialize` reconfigures every singleton and reloads state; every test initialises from a fresh temp directory.
- [BCrypt cost slows tests] → Cost is injected; tests use 4 rounds.
- [Assembly plugin replaces the main artifact (warning)] → Intended; the file name `amusement-park.jar` stays stable.
