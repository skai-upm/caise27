# Design

## Context

After E2 every class is in `com.amusementpark`. `Park` holds the date, price, all collections and all cross-entity rules. `App` builds `Park` with a data directory, which is the normal working directory or a `--fly` temp directory. Repositories are concrete classes, one per file. Tests live alongside in the same package. This change restructures that code and adds features (see proposal.md). Requirements are in `specs/`.

## Goals / Non-Goals

**Goals:**
- Three layers in separate packages with one-way dependencies: presentation → business → persistence → business.model (persistence only needs the model to (de)serialise it).
- Small singleton controllers, each owning one responsibility, wired once at start-up.
- Patterns used where the delivery asks: Builder (invoices), Singleton (controllers), dependency injection (repositories, collaborators, help format, password hasher, invoice builder factory), Strategy (help format).
- Keep every E1/E2 command and output unchanged.

**Non-Goals:**
- Frameworks or DI containers; wiring is plain Java in a bootstrap class.
- Persisting invoices, attraction occupants, system date or price.

## Decisions

### Packages
- `presentation`: `App` (main, mode selection, `--fly`), `Bootstrap` (creates repositories for a data dir, configures and wires singletons, loads data, returns a `CommandProcessor`), `CommandProcessor`, and `presentation.help` (`CommandInfo`, `CommandCatalog`, `HelpFormat` interface, `SyntaxHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`).
- `business.model`: `User`, `UserField`, `Group`, `Admission` (interface), `Ticket`, `IndividualTicket`, `GroupTicket`, `Pass`, `SpeedPass`, `SpecialPass`, `AttractionType`, `Attraction<T extends Ticket>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`.
- `business.controller`: `ParkController` (date, price), `UserController`, `GroupController`, `TicketController` (individual/group tickets and passes), `AttractionController`, `InvoiceController`.
- `business.invoice`: `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` (interface), `StandardInvoiceBuilder`.
- `business.validation`: `Validator`. `business.security`: `PasswordHasher` (BCrypt wrapper).
- `persistence`: repository interfaces (`UserRepository`, `GroupRepository`, `TicketRepository`, `GroupTicketRepository`, `PassRepository`, `AttractionRepository`) and file implementations (`FileUserRepository`, …), plus the package-private `References` helper.
- Tests move to the matching packages. The manifest main class becomes `com.amusementpark.presentation.App`.

*Alternative:* keep concrete repositories without interfaces — rejected because the delivery explicitly anticipates different repository implementations.

### Singleton + injection
Each controller has `private static final X INSTANCE`, a private constructor, `getInstance()`, and a `configure(...)` method taking its repositories/services and collaborators, which also resets its state. A `load()` method then reads its data. `Bootstrap.initialize(dataDir, today, hasher)` performs, in order: create repositories → configure `ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController` with their collaborators → load users, groups, tickets (group tickets, tickets, passes), attractions → return a `CommandProcessor` whose `HelpGenerator` is built with the formats map. Re-running `initialize` (tests, `--fly`) reconfigures the same singleton instances. Controllers never call `new` on repositories or other controllers.

Collaborations: `UserController` → `TicketController` (deletion lock), `GroupController` (remove deleted user); `GroupController` → `UserController`, `TicketController` (lock); `TicketController` → `ParkController`, `UserController`, `GroupController`; `AttractionController` → `ParkController`, `TicketController`; `InvoiceController` → `UserController`, `GroupController`, `TicketController`, `Supplier<InvoiceBuilder>`.

### Builder for invoices
`InvoiceBuilder` steps: `forRecipient(String)`, `addIndividualTickets(List<IndividualTicket>)`, `addGroupTickets(List<GroupTicket>)`, `addSpeedPasses(List<SpeedPass>)`, `addSpecialPasses(List<SpecialPass>)`, `build()`. Each `add…` step adds a section only when the list is non-empty. `InvoiceController` is the director: a user invoice calls recipient → individual tickets (bought directly) → speed passes → special passes (of all the user's individual tickets, including group-generated ones); a group invoice calls recipient → group tickets. `Invoice.render()` prints sections, item prices and `TOTAL: x.xx`. A new builder comes from the injected `Supplier<InvoiceBuilder>` for each invoice.

### Help
`CommandCatalog.commands()` returns the ordered `CommandInfo(syntax, description)` registry. `HelpGenerator(List<CommandInfo>, Map<String, HelpFormat>)` renders the variant requested (`""` → `SyntaxHelpFormat`, `"EXTENDED"` → `ExtendedHelpFormat`). `CommandProcessor` receives the generator by constructor injection and maps `HELP` / `HELP EXTENDED` to it.

### Passwords
`org.mindrot:jbcrypt:0.4`. `PasswordHasher(int logRounds)` exposes `hash`, `matches`, `isHash` (prefix `$2a$`/`$2b$`/`$2y$` and length 60). Production uses 10 rounds; tests inject 4 for speed. `UserController` validates the password format and then hashes it. On `load()`, any non-hash password is hashed and the file is rewritten (migration of E1/E2 data). The `maven-shade-plugin` builds the executable JAR with jbcrypt included.

### Passes
`abstract class Pass { id; price; consumed; }` with `SpeedPass` and `SpecialPass`. `IndividualTicket` owns `List<SpeedPass> speedPasses` and `SpecialPass specialPass` (composition, since passes exist only through their ticket). Ids are `<ticketId>-SP<n>` (n = highest existing + 1) and `<ticketId>-SPECIAL`. SpeedPass price = ticket price × 0.20 (HALF_UP, 2 decimals). Special Pass price = ticket price. Passes can be bought only for tickets dated on or after the system date (consistent with ticket purchase), and refunded only when the ticket date is strictly after it. After a refund the ticket has no Special Pass, so another one may be bought ("at most one" means at most one existing).

`passes.txt`: `passId;SPEED|SPECIAL;ticketId;price;consumed(true|false)`, loaded after all tickets and attached by ticket id. Lines whose ticket is unknown are skipped.

### Attractions with generics
`interface Admission { List<IndividualTicket> admittedTickets(); default List<User> getUsers() }` is implemented by `Ticket` (`IndividualTicket` → itself; `GroupTicket` → its generated tickets). `abstract class Attraction<T extends Ticket>` holds `Class<T> admitted` and offers `T admit(Ticket)` (type check + cast, `null` if not admitted), `String checkEntry(T)` (presence/capacity), `void enter(T)`, `int exit(Ticket)`, `boolean allowsSpeed()`, `boolean requiresSpecialPass()`, `AttractionType getType()`. Subclasses: `GeneralAttraction extends Attraction<Ticket>`, `IndividualAttraction extends Attraction<IndividualTicket>`, `GroupAttraction extends Attraction<GroupTicket>`, `SpecialAttraction extends Attraction<IndividualTicket>` (`allowsSpeed=false`, `requiresSpecialPass=true`). `AttractionType.create(id, name, capacity)` is the factory.

`AttractionController.enter` runs these steps: find the attraction and ticket; `admit` checks the type; check the date; check SPEED / Special Pass availability (collect the passes to consume); `checkEntry`; then `enter`, and only then mark the passes consumed and save `passes.txt`. Any failure before `enter` changes nothing. Because occupants are not persisted, a pass-save failure after entry is reported but the entry stands.

### Cascading deletion
`TicketController.deleteTicket/deleteGroupTicket` validate first, then remove the ticket(s) from memory and save tickets/group tickets and passes. If a save fails, the removed objects are restored in memory and the files are rewritten from the restored state, so no partial deletion remains.

### File compatibility
`attractions.txt` gains a 4th column `type`; 3-column lines load as GENERAL. `users.txt` keeps its layout with the hash in the password column.

### Commit labels
The delivery names ten mandatory commits. In this batch run the lifecycle is committed once per proposal, and the SDDX apply metrics measure the uncommitted diff. So the ten labels go into the single commit message, and each task group below maps to one label.

## Risks / Trade-offs

- [Singletons hinder test isolation] → `configure` resets state; tests call `Bootstrap.initialize` with a temp dir before each test, and the surefire tests run in one thread.
- [BCrypt slows tests] → Injected hasher with 4 log rounds in tests.
- [Large mechanical move of classes] → Done first (C01) with the full test suite green before features are added.
- [Pass save failure after entry] → Reported as an error; occupancy is in memory only, so this is a minor inconsistency.
