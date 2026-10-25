# Design

## Context

Delivery 2 code lives in `es.upm.etsisi.park.{app,command,controller,model,persistence,service,validation}`. `ParkController` is a singleton that owns all state and persists through `ParkStorage`; `CommandProcessor` maps commands to it; business errors are `UserException` (unchecked); passwords are stored in plain text; attractions are untyped; there are no passes or invoices. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Visible three-layer package structure with no file access from presentation and no persistence in domain objects.
- Unique-responsibility controllers as singletons, wired by a hand-written composition root; repositories are ordinary injected objects.
- Bounded generics on attractions that really constrain the accepted ticket kind.
- Atomic entry/pass consumption and atomic cascade deletions.

**Non-Goals:**
- Migrating plaintext `users.txt` from earlier deliveries.
- Persisting invoices, occupancy, system date or price.
- Course milestone commits (see proposal Impact).

## Decisions

### Package layout
Root `es.upm.etsisi.park`:
- `presentation` — `App` (entry point and composition root), `CommandRunner`, `CommandProcessor`, `CommandResult`, `TemporaryDirectory`; `presentation.help` — `CommandCatalog` (syntax + description per command), `HelpFormatter` (strategy interface), `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator` (receives catalog and formatter by constructor).
- `business.model` — domain classes (users, groups, tickets, passes, attractions, access interfaces, enums).
- `business.controller` — singleton controllers.
- `business.invoice` — `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` (abstract builder), `UserInvoiceBuilder`, `GroupInvoiceBuilder`.
- `business.exception` — `ParkException` (unchecked base) and `ValidationException`, `NotFoundException`, `DuplicateException`, `AuthenticationException`, `OperationNotAllowedException`, `AccessDeniedException`.
- `business.security` — `PasswordHasher` interface and `BCryptPasswordHasher` (jBCrypt).
- `business.validation` — `UserValidator`.
- `persistence` — repository interfaces (`UserRepository`, `GroupRepository`, `TicketRepository`, `PassRepository`, `AttractionRepository`), their `*FileRepository` implementations, `TextFiles`, `ParkStorage` (factory for one data directory), `PersistenceException`.

Alternative: one package per entity (vertical slices) — rejected because the assignment asks for layers.

### Exceptions
All business failures throw subclasses of `ParkException` from controllers/model. `CommandProcessor` catches `ParkException` and `PersistenceException` and renders `ERROR: <message>`; argument-count problems throw `ValidationException` with the usage. A final `RuntimeException` catch keeps the loop alive on bugs. Unchecked exceptions keep controller signatures readable; the hierarchy lets tests assert the kind of failure.

### Controllers and dependency injection
Singletons (private constructor, `getInstance()`, `initialize(...)` that receives dependencies and loads state):
- `SystemController` — current date and price.
- `UserController(UserRepository, PasswordHasher)` — registry, create/update/authenticate/remove (replaces `UserService`).
- `GroupController(GroupRepository, UserController)`.
- `TicketController(TicketRepository, UserController, SystemController, groupLookup)` — ticket registry, shared identifier space, activity/lock queries.
- `PassController(PassRepository, TicketController, SystemController)` — pass registry, purchase, refund, cascade removal; attaches passes to their tickets on load.
- `AttractionController(AttractionRepository, TicketController, SystemController, PassController)` — creation, typed entry, exit, queries.
- `InvoiceController(UserController, GroupController, TicketController, PassController)` — the Builder director.
- `ParkController` — the Delivery 2 central object, now a coordinator for cross-entity rules (user deletion, group lock checks, ticket deletion cascades, group deletion detaching tickets) and the single façade used by `CommandProcessor`.

`App` is the composition root: builds `ParkStorage` for the normal or `--fly` directory, creates `BCryptPasswordHasher`, initialises controllers in dependency order and injects them into `ParkController`, then builds `HelpGenerator`s and `CommandProcessor`. Tests use the same `initialize` methods with temporary directories and a low-cost hasher. Circular controller dependencies are avoided by letting `ParkController` coordinate operations that span entities.

### Passwords
`User` stores `passwordHash`. `UserController` validates the plaintext with `UserValidator`, hashes with the injected `PasswordHasher`, and verifies with `hasher.matches`. `BCryptPasswordHasher` uses `BCrypt.gensalt(10)` by default (tests inject 4 rounds); a stored value that is not a BCrypt hash makes `matches` return false instead of throwing. Users file keeps the same column layout with the hash in the password column.

### Tickets, access interfaces and generics
- `AccessTicket` (general access capability): `getHolders()`, `isValidOn(date)`, `getAccessTickets()` (the individual tickets that enter).
- `IndividualAccess extends AccessTicket` — implemented by `IndividualTicket`.
- `GroupAccess extends AccessTicket` — implemented by `GroupTicket`.
- `Attraction<T extends AccessTicket>` (abstract) holds id, name, capacity, occupants and `Class<T> accessType`. `accepts(Ticket)` uses `accessType`; `enter(T ticket, boolean speed)` is the template method: validate date, capacity and duplicates, call hook `selectPasses(T, speed)` that returns the passes to consume (or throws), then admit everyone and consume the passes — so a failure leaves state untouched.
- `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (overrides the hook: rejects SPEED, requires an unconsumed Special Pass). INDIVIDUAL and SPECIAL share the same ticket interface; only behaviour differs.
- `AttractionType` enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) creates the right subclass and is persisted.
- `AttractionController.enter` captures the wildcard with a generic helper `<T extends AccessTicket> enter(Attraction<T>, Ticket, speed)` that casts via `accessType` after `accepts` succeeded.

### Passes
`Pass` (abstract: id, price, `IndividualTicket ticket`, consumed) with `SpeedPass` and `SpecialPass`. `IndividualTicket` keeps its passes (`List<SpeedPass>`, `SpecialPass` 0..1) so access checks and invoices navigate ticket → passes; the pass keeps its ticket (bidirectional, required by the assignment). Prices are computed at purchase from `ticket.getPrice()` (20 % / 100 %, scale 2) and stored. `PassController` keeps a `LinkedHashMap<String, Pass>`; package purchase validates quantity, ticket kind/date and all ids before creating anything. Refund requires unconsumed and `ticket.isDeletableOn(today)`. A refunded Special Pass leaves the ticket without one, so another may be bought ("at most one" counts passes that exist, consumed or not).

### Cascades
`ParkController.deleteTicket/deleteGroupTicket` first ask `TicketController` to validate (exists, top-level kind, strictly future); only then `PassController.removeAllFor(tickets)` and `TicketController.remove`. Both persist after the in-memory change.

### Invoices (Builder)
`InvoiceBuilder` (abstract) offers the common steps `title(...)`, `recipient(label, value)`, protected `section(title, lines)` that skips empty sections, `informational(...)` lines with no amount, and `build()` which sums only priced lines. `UserInvoiceBuilder` adds `user(User)`, `individualTickets(...)`, `speedPasses(...)`, `specialPasses(...)`; `GroupInvoiceBuilder` adds `group(Group)` and `groupTickets(...)` (one priced line per group ticket plus informational composition lines). `InvoiceController` acts as director: it selects data (direct individual tickets of the user, passes of every individual ticket held by the user, group tickets whose group is the target) and calls only the steps that apply. `Invoice.toText()` renders sections and the total.

### Help
`CommandCatalog` lists every command's syntax and description in display order. `HelpGenerator(CommandCatalog, HelpFormatter)` produces text; `CommandProcessor` receives two generators (basic and extended) by constructor, so new formats need no change to command dispatch.

### Persistence formats
- `attractions.txt`: `id;name;capacity;TYPE` (3-field lines load as `GENERAL`).
- `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;consumed(true|false)`; ticket ids may be component ids. Loaded after tickets.
- Other files unchanged except the password column now holding a hash.

### Build
Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` 3.6.0 (`package` phase, `ManifestResourceTransformer` with the new main class `es.upm.etsisi.park.presentation.App`) so `target/amusement-park.jar` contains jBCrypt.

### Output
New messages: `SPEEDPASS PURCHASED: <n> PASS(ES) <first>..<last>. TOTAL PRICE: <p> EUR`, `SPECIAL PASS PURCHASED. PRICE: <p> EUR`, `SPEEDPASS REFUNDED`, `SPECIAL PASS REFUNDED`, entry suffixes `. SPEEDPASS CONSUMED: <ids>` / `. SPECIAL PASS CONSUMED: <id>`, and multi-line invoices.

## Risks / Trade-offs

- [Large package move] → done mechanically first; the full Delivery 1–2 test suite must pass before new features.
- [BCrypt slows tests] → injectable hasher with 4 log rounds in tests.
- [Plaintext `users.txt` from older runs] → such users cannot authenticate (no migration required); documented in README.
- [Singleton state across tests] → every test re-initialises controllers with a fresh temporary directory.
- [Interpretation: refunded Special Pass frees the slot] → refunds remove the pass from the system, so no record remains to block a new purchase.
