# Design

## Context

Delivery 2 code lives in one package `es.upm.etsisi.amusementpark`: `App` (args, `--fly`), `CommandProcessor`, singleton `ParkController` (date, price, groups, tickets, attractions, USER DELETE), `UserService` (users), domain classes, `*Repository` classes over `TextFiles`, `Storage`. See proposal.md - Why.

## Goals / Non-Goals

**Goals:** layered packages with one-way dependencies (presentation → business ← persistence implementations), Singleton controllers wired by a hand-written composition root, Builder invoices, generics that really restrict attraction access, BCrypt passwords, and full backward compatibility of command behaviour.

**Non-Goals:** migrating plaintext password files; deleting attractions; persisting invoices.

## Decisions

### Packages
```
es.upm.etsisi.amusementpark
├── App                       bootstrap / composition root (args, --fly, wiring)
├── presentation              CommandProcessor, CommandCatalog, CommandInfo,
│                             HelpGenerator, HelpFormat, BasicHelpFormat,
│                             ExtendedHelpFormat, InvoicePrinter
├── business
│   ├── model                 User, UserField, UserValidator, Group, Ticket,
│   │                         IndividualTicket, GroupTicket, GroupDiscount,
│   │                         GeneralAccess, IndividualAccess, GroupAccess,
│   │                         Pass, SpeedPass, SpecialPass
│   ├── model.attraction      Attraction<T>, AttractionType, GeneralAttraction,
│   │                         IndividualAttraction, GroupAttraction, SpecialAttraction, EntryResult
│   ├── controller            UserController, ParkController, PassController, InvoiceController
│   ├── invoice               Invoice, InvoiceSection, InvoiceLine, InvoiceBuilder,
│   │                         UserInvoiceBuilder, GroupInvoiceBuilder
│   ├── repository            UserRepository, GroupRepository, TicketRepository,
│   │                         AttractionRepository, PassRepository (interfaces)
│   ├── security              PasswordHasher, BCryptPasswordHasher
│   └── exception             ParkException (+ subclasses)
└── persistence               Storage, TextFiles, Text*Repository implementations
```
Repository interfaces live in business so that business never depends on persistence; persistence implements them (dependency inversion). Only `App` knows the concrete classes.

### Exceptions
Checked base `ParkException` with `ValidationException` (bad input values), `NotFoundException`, `ConflictException` (duplicates / identifier clashes), `ForbiddenOperationException` (state rules: locks, dates, access types, passes) and `AuthenticationException`. Persistence failures raise unchecked `PersistenceException` (business.exception) caught by `CommandProcessor`/`App`. `CommandProcessor` is the single place that converts exceptions to `ERROR: ...` lines.

### Singletons and injection
`UserController`, `ParkController`, `PassController`, `InvoiceController`: private constructor, eager static instance, `getInstance()`, and a `configure(...)` method receiving dependencies:
- `UserController.configure(UserRepository, PasswordHasher)`
- `PassController.configure(PassRepository)`
- `ParkController.configure(LocalDate today, UserController, PassController, GroupRepository, TicketRepository, AttractionRepository)` — loads state, then `PassController.load(ticketLookup)`.
- `InvoiceController.configure(UserController, ParkController, PassController)`
`App.bootstrap(Storage, LocalDate, PasswordHasher)` creates text repositories for the given storage and configures all controllers, then builds `CommandProcessor` with two injected `HelpGenerator`s (basic and extended). Repositories are plain objects created per bootstrap (never singletons). Tests use the same bootstrap with a temp directory and a low-cost hasher, or in-memory repositories.

### Passwords
`PasswordHasher` (hash, matches) with `BCryptPasswordHasher(int logRounds)` using `org.mindrot.jbcrypt.BCrypt`; production uses 10 rounds, tests 4. `UserController` validates, hashes on create and on PASSWORD update, and verifies with `matches` (invalid stored hash → no match). `User` no longer compares passwords itself. maven-shade-plugin 3.6.0 builds the shaded executable `target/amusement-park.jar`.

### Ticket access interfaces and generic attractions
`GeneralAccess` (`getId`, `isValidOn(LocalDate)`, `entryTickets(): List<IndividualTicket>`); `IndividualAccess extends GeneralAccess`; `GroupAccess extends GeneralAccess`. `IndividualTicket implements IndividualAccess` (entry tickets = itself), `GroupTicket implements GroupAccess` (entry tickets = composition).
`abstract class Attraction<T extends GeneralAccess>` keeps id/name/capacity/occupants and `accessType(): Class<T>`. Public `enter(Ticket, boolean speed, LocalDate, PassController)` checks `accessType().isInstance(ticket)` and delegates to `admit(T access, ...)`, which: checks validity date, asks the subclass hook `passesFor(List<IndividualTicket>, boolean speed, PassController)` for the passes to consume (throwing if missing), checks occupants/capacity, then consumes passes and adds occupants — all checks before any mutation (atomic). Subclasses: `GeneralAttraction extends Attraction<GeneralAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (overrides the hook: rejects SPEED, requires an unconsumed Special Pass). `AttractionType` enum creates the right subclass. INDIVIDUAL and SPECIAL share the ticket type; the Special Pass rule is attraction behaviour.

### Passes
`abstract Pass` (id, `IndividualTicket ticket`, price, consumed; `consume()`); `SpeedPass` price = ticket price × 20 %; `SpecialPass` price = ticket price. `PassController` holds all passes, enforces the shared id space, quantity 1/10, `<purchase>:<n>` ids (all checked first), one Special Pass per ticket, refund rules, `firstUnconsumedSpeedPass(ticket)` (earliest purchased), `specialPassOf(ticket)`, `passesOf(ticket)` and `removePassesOf(tickets)`. `ParkController.deleteTicket/deleteGroupTicket` validate first, then remove passes and tickets, then save.

### Invoices (Builder)
`Invoice` (title, header lines, sections, total) is immutable; `InvoiceSection(title, lines, details)`; `InvoiceLine(description, amount)`. Abstract `InvoiceBuilder` holds common steps (`title`, `headerLine`, `addSection`, `build()` computing the total from line amounts and omitting empty sections). `UserInvoiceBuilder`: `forUser(User)`, `withTickets(List<IndividualTicket>)`, `withSpeedPasses(List<SpeedPass>)`, `withSpecialPasses(List<SpecialPass>)`. `GroupInvoiceBuilder`: `forGroup(Group)`, `withGroupTickets(List<GroupTicket>)` (composition as detail lines without amounts). `InvoiceController` is the director gathering data; `InvoicePrinter` (presentation) renders text.

### Help
`CommandCatalog` lists `CommandInfo(syntax, description)` for every command. `HelpFormat` strategy (`String format(List<CommandInfo>)`); `BasicHelpFormat` (syntax only), `ExtendedHelpFormat` (syntax + purpose). `HelpGenerator(HelpFormat)` is created in bootstrap and injected into `CommandProcessor`.

### Persistence
- `attractions.txt`: `id;name;capacity;TYPE` (3-field lines → GENERAL).
- `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;consumed` — resolved against all tickets (including composition); unresolved lines skipped with a warning.
- Load order: users → attractions → groups → tickets → passes. `--fly` reuses the same repository classes with the temp `Storage`.

### UML
Update `uml/model.puml` with packages, interfaces, generics (`Attraction<T extends GeneralAccess>`), pass cardinalities (`IndividualTicket "1" <-- "0..*" SpeedPass`, `"0..1" SpecialPass`), builders, singletons (`<<singleton>>`) and injected dependencies.

## Risks / Trade-offs

- [Large file move] → Behaviour-preserving moves first; full test suite after each group.
- [BCrypt cost slows tests] → Injected hasher with low cost in tests.
- [Singleton state between tests] → `configure` fully reinitialises each controller.
- [Plain-text legacy users] → Documented; they fail authentication instead of crashing.
