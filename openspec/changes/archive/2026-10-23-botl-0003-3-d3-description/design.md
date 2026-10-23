# Design

## Context

Delivery 2 code (see the archived change `2026-10-23-botl-0002-2-d2-description`):
- packages `model`, `service` (`Park` singleton holding every map, `UserService`, exceptions), `validation`, `persistence` (`ParkStore` plus one file repository per entity, `TextFiles.writeAll` for all-or-nothing writes, `TemporaryDataDirectory`), and `command` (table-driven `CommandProcessor`, `CommandRunner`)
- `App`, which handles `--fly`
- 241 JUnit tests

Delivery 3 restructures these and adds features (see proposal.md and the specs).

## Goals / Non-Goals

**Goals:**
- Keep all Delivery 1 and 2 behavior and tests (adapted to new packages and APIs) passing.
- Make each pattern the assignment asks for visible and checkable in the code: Singleton, DI, Builder, Strategy for help, bounded generics.
- Keep all-or-nothing persistence and undo-on-failed-save for every mutating command, including cascades and atomic group entry.

**Non-Goals:**
- Migrating Delivery 1 and 2 `users.txt` files with plain passwords. Such lines are skipped with a warning, which the assignment allows.
- Persisting invoices or attraction occupants.
- The mandatory `[F3-Cxx]` milestone commits. BOTL makes one commit per proposal.

## Decisions

**Package layout (root `es.upm.amusementpark`).**
- `App`: the entry point and composition root. It parses `--fly`, chooses the data directory, creates repositories, wires controllers and presentation, and runs input. It is the only class that knows every layer.
- `presentation`: `CommandProcessor`, `CommandRunner`, `CommandResult`, `CommandInfo`, `HelpGenerator`, `HelpFormatter` (with `SyntaxHelpFormatter` and `ExtendedHelpFormatter`), and `InvoiceFormatter`. It depends only on `business`.
- `business.model`:
  - `User`, `UserField`, `Group`
  - access interfaces `GeneralAccess`, `IndividualAccess`, `GroupAccess`
  - `Ticket`, `IndividualTicket`, `GroupTicket`, `GroupDiscount`
  - `Pass`, `SpeedPass`, `SpecialPass`
  - `Attraction<T extends GeneralAccess>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`
  - `PassWallet` (an interface)
- `business.controller`: `Park`, `SystemController`, `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController`, and `EntryResult`.
- `business.exception`: `ParkException` (checked, abstract), `InvalidDataException`, `NotFoundException`, `OperationNotAllowedException`, and `PersistenceException`.
- `business.invoice`: `Invoice`, `InvoiceSection`, `InvoiceItem`, the `InvoiceBuilder<B>` interface, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, and `GroupInvoiceBuilder`.
- `business.security`: `PasswordHasher` (an interface) and `BCryptPasswordHasher`.
- `business.validation`: `UserValidator`.
- `persistence`:
  - repository interfaces `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, and `PassRepository`
  - `File*Repository` implementations
  - `FileDataStore` (implements `TransactionManager`), `TextFiles`, and `TemporaryDataDirectory`

**Controllers as Singletons, wired by hand.**
- Every controller has a private constructor, `static getInstance()`, and `configure(...)`, which receives repositories, the `TransactionManager`, and collaborating controllers.
- `App.createPark(dataDir, warnings, today, hasher)` builds a fresh `FileDataStore` and fresh repository instances for that directory. It then configures every controller and calls `Park.load()`, which loads users, groups, attractions, tickets, and passes in reference order.
- Because configuration is a separate step, the same singletons can be re-wired for `--fly` and in tests, and the cyclic collaborations work: users depend on tickets, groups, and attractions, and vice versa. Repositories are plain objects, never singletons.
- *Alternative considered:* constructor injection with non-singleton controllers. Rejected because the assignment requires Singleton controllers.

**`Park`** stays the central Delivery 2 controller. It exposes the controllers (`system()`, `users()`, `groups()`, `tickets()`, `passes()`, `attractions()`, `invoices()`) and orchestrates `load()`. Presentation receives `Park` by injection.

**Transactions across repositories.** `FileDataStore.inTransaction(IoAction)` stages each repository's `saveAll` as file contents, and `TextFiles.writeAll` writes them together at the end, keeping Delivery 2's all-or-nothing guarantee. Outside a transaction, a save writes immediately.

Controllers persist through a shared `AbstractController.persist(IoAction save, Runnable undo)`. On `IOException` it runs `undo` and throws `PersistenceException`. Multi-entity commands save inside one transaction:
- `USER DELETE`: users and groups
- ticket deletion: tickets and passes
- speed or special entry: passes

**Exceptions.**
- `InvalidDataException`: syntax and validation.
- `NotFoundException`: unknown user, group, ticket, pass, or attraction.
- `OperationNotAllowedException`: locks, dates, capacity, wrong password, type restrictions, missing passes.
- `PersistenceException`: save failures.

`CommandProcessor` catches `ParkException` per command and prints `ERROR: <message>`, as before.

**BCrypt.**
- `PasswordHasher` has `hash(plain)` and `matches(plain, hash)`. `BCryptPasswordHasher(int logRounds)` uses `BCrypt.gensalt(logRounds)`: the default 10 in `App`, and the library minimum 4 in tests to keep the suite fast.
- `User` stores `passwordHash` only. `UserController` validates the plain password with `UserValidator`, then hashes it, and checks credentials with `hasher.matches`.
- `FileUserRepository` accepts a password field only if it looks like a BCrypt hash (`$2a$`, `$2b$`, or `$2y$` prefix and 60 characters). Otherwise the line is skipped with a warning.
- jBCrypt 0.4 is a compile-scope dependency. `maven-shade-plugin` 3.6.0 (`package` phase, `ManifestResourceTransformer` with the main class) replaces `target/amusement-park.jar` with a self-contained JAR. `maven-jar-plugin` keeps its manifest.

**Invoices (Builder plus Director).**
- `InvoiceBuilder<B extends InvoiceBuilder<B>>` declares the common steps: `issuedOn(LocalDate)`, `section(String title, List<InvoiceItem> items)`, which skips an empty list, and `build()`.
- `AbstractInvoiceBuilder<B>` implements them: it collects sections and computes the total from counted items.
- `UserInvoiceBuilder` adds `customer(User)`, `individualTickets(...)`, `speedPasses(...)`, and `specialPasses(...)`.
- `GroupInvoiceBuilder` adds `group(Group)` and `groupTickets(...)`. Each group ticket becomes an `InvoiceItem` with the total price and the composition as non-counted detail lines.
- `InvoiceController` is the director. It gathers data by traversing tickets and passes and calls only the steps that have data.
- `Invoice` is immutable. `InvoiceFormatter` (presentation) renders it as text: a title, a header, sections with items `id … amount EUR` and detail lines, and `TOTAL: x.xx EUR`.

**Help (Strategy injected into a generator).**
- `CommandProcessor` registers each command with one or more `CommandInfo(syntax, description)` entries. `ATTRACTION CREATE`, `ATTRACTION ENTER`, and `INVOICE CREATE` have two syntaxes each, and accepted argument counts come from all of a command's syntaxes.
- `HelpGenerator(HelpFormatter)` takes the formatter in its constructor and produces text from the list of `CommandInfo`.
- `App` injects two generators into `CommandProcessor`: one with `SyntaxHelpFormatter` for `HELP`, one with `ExtendedHelpFormatter` for `HELP EXTENDED`.
- `HELP EXTENDED` is its own keyword because the keyword is the whole first element.

**Passes.**
- `abstract Pass` holds `id`, `IndividualTicket ticket`, `price` (`BigDecimal`), and `consumed`. It has `consume()`, `revertConsumption()` (undo only), `isRefundableOn(today)`, which is `!consumed && ticket.isDeletableOn(today)`, and `abstract kind()`.
- `SpeedPass.priceFor(ticket)` is 20% half-up. `SpecialPass.priceFor(ticket)` is the ticket price.
- Passes reference their ticket (`Pass → IndividualTicket`, many-to-one). Tickets do not reference passes, so a ticket's passes are found by traversing `PassController`. The UML shows the multiplicities `IndividualTicket "1" ← "0..*" SpeedPass` and `"0..1" SpecialPass`.
- `PassController` implements `PassWallet` (`availableSpeedPass(ticket)` and `unconsumedSpecialPass(ticket)`) for attractions. It offers `removeFor(tickets)`, which returns the removed passes in order for undo, for the ticket cascades.

**Attractions with bounded generics.**
- `GeneralAccess` has `getId()`, `isValidOn(LocalDate)`, `getHolders()`, and `getEntryTickets()`, the individual tickets entering: itself or its composition.
- `IndividualAccess extends GeneralAccess` adds `getUser()`.
- `GroupAccess extends GeneralAccess` adds `getComposition()`.
- `Ticket` implements `GeneralAccess`, `IndividualTicket` implements `IndividualAccess`, and `GroupTicket` implements `GroupAccess`.
- `abstract class Attraction<T extends GeneralAccess>` keeps Delivery 2's occupancy logic and adds:
  - `Class<T> accessType` and `accepts(GeneralAccess)`, using `accessType.isInstance`
  - `T admit(GeneralAccess)`: a checked cast, which throws `OperationNotAllowedException` when the attraction does not accept the ticket
  - the abstract `List<Pass> requiredPasses(T ticket, boolean speed, PassWallet wallet)`, which validates options and returns the passes to consume
  - `enter(T ticket, List<Pass> passes)`, which adds holders and consumes passes
- Subclasses:
  - `GeneralAttraction extends Attraction<GeneralAccess>`: SPEED takes one speed pass per entry ticket.
  - `IndividualAttraction extends Attraction<IndividualAccess>`: SPEED takes the ticket's speed pass.
  - `GroupAttraction extends Attraction<GroupAccess>`: SPEED takes one per composition ticket, via `getComposition()`.
  - `SpecialAttraction extends Attraction<IndividualAccess>`: SPEED is rejected, and the ticket's unconsumed Special Pass is required.
- `AttractionType` (enum) creates instances and is persisted.

**Atomic entry.** `AttractionController.enter(attractionId, ticketId, option)` works in two phases:
1. Checks, with no mutation: the option is empty or `SPEED`; the ticket exists and is valid today; the attraction accepts the ticket type (`admit`); holders are active users and not inside any attraction; capacity; `requiredPasses`.
2. Enter all holders, consume the passes, and persist the passes. On save failure, undo exits and consumptions.

It returns an `EntryResult` (user count and consumed pass ids) for the output text.

**Persistence formats.**
- `attractions.txt`: `id;name;capacity;TYPE`. Three-field lines load as GENERAL.
- `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;consumed(true|false)`, loaded after tickets and resolving the ticket by id (including group components). Unknown tickets, duplicates, or a second Special Pass on the same ticket are skipped with a warning.

**Source example interpretation.** As before, the line-wrapped example passwords (`Clave1` / `!`) are typesetting artifacts.

## Risks / Trade-offs

- [Large package move breaks every import] → Move mechanically first and run the full suite before adding features (task group 1).
- [Singletons hold state across tests] → `configure` plus `load` fully reset each controller. Tests always build the park through the same `App` wiring helper.
- [The BCrypt cost slows tests] → Inject the 4-round hasher in tests; production uses 10.
- [The shaded JAR changes the artifact] → The integration task inspects the JAR and runs it with `java -jar` from a scratch directory.
- [Cyclic controller collaborations] → `configure` runs before any use. Only `load` and command methods call collaborators.
