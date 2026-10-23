# Design

## Context

After Delivery 2 the code lives in `es.upm.etsisi.park` with sub-packages `cli`, `user`, `model.*` and `persistence`. A single `Park` singleton owns all state and rules (groups, tickets, attractions, settings) and delegates user CRUD to `UserService`; `ParkStorage` builds four concrete text repositories for a data directory; `CommandProcessor` is one large `switch`. Constraints for Delivery 3 (see proposal.md): explicit presentation/business/persistence packages; singleton controllers but non-singleton, injected, replaceable repositories; hand-written DI; Builder for invoices; bounded generics + interfaces for attraction access; exceptions as part of the design; BCrypt via Maven; `--fly` only changes repository configuration.

## Goals / Non-Goals

**Goals:**
- Clear layer boundaries: presentation never touches files; domain objects never persist themselves.
- One controller per managed concept, each a singleton configured once at start-up with its repository and collaborating controllers.
- Keep every Delivery 1–2 behaviour (except the explicitly changed ones) and its tests.

**Non-Goals:**
- Migrating plain-text passwords from earlier `users.txt` files (explicitly not required).
- Cross-file transactional persistence.
- Persisting invoices.

## Decisions

### Package layout
```
es.upm.etsisi.park
├── App                      bootstrap: arguments, --fly directory, wiring (composition root)
├── ApplicationContext       builds and connects controllers/repositories (DI by hand)
├── presentation             CommandProcessor, CommandRegistry, CommandDefinition, command groups
│   └── help                 HelpGenerator, HelpRenderer, SyntaxHelpRenderer, ExtendedHelpRenderer
├── business
│   ├── exception            ParkException + subclasses
│   ├── controller           SystemController, UserController, GroupController, TicketController,
│   │                        PassController, AttractionController, InvoiceController
│   ├── security             PasswordHasher, BCryptPasswordHasher
│   └── model
│       ├── user             User, UserField, UserValidator
│       ├── group            Group
│       ├── ticket           Ticket, IndividualTicket, GroupTicket, GroupDiscount, Money,
│       │                    Access, IndividualAccess, GroupAccess
│       ├── pass             Pass, SpeedPass, SpecialPass
│       ├── attraction       Attraction<T extends Access>, AttractionType, GeneralAttraction,
│       │                    IndividualAttraction, GroupAttraction, SpecialAttraction, Entry
│       └── invoice          Invoice, InvoiceSection, InvoiceLine, InvoiceBuilder,
│                            UserInvoiceBuilder, GroupInvoiceBuilder
└── persistence              UserRepository, GroupRepository, TicketRepository, PassRepository,
    │                        AttractionRepository (interfaces)
    └── text                 TextUserRepository, ..., TextFiles, TextRepositories (factory for a directory)
```
`App` and `ApplicationContext` stay in the root package: they are the composition root, not part of a layer.

### Controllers as singletons, repositories injected
Each controller has a private constructor, a `private static final` instance and `getInstance()`. `ApplicationContext.initialize(Repositories, PasswordHasher)` calls `configure(...)` on each controller, passing the repository interface and the collaborating controllers (setter-style, so mutual collaborations such as User↔Ticket↔Group need no construction order), then loads data in dependency order: users → groups → tickets → passes → attractions. Re-calling `initialize` (tests, a new run) replaces all state. Repositories are ordinary objects created by `TextRepositories.in(directory)`; normal and `--fly` runs differ only in that directory, and tests could inject other implementations. Alternative considered: keeping `Park` as a facade over controllers — rejected because it would keep a god-object in front of the layers the assignment asks to make visible.

Controller responsibilities (summary):
- `SystemController`: system date, base price, date parsing.
- `UserController`: create/update/delete/authenticate; uses `PasswordHasher`; for delete asks `TicketController.hasActiveTicket` and `GroupController.isLocked`, then `GroupController.removeFromAllGroups` and `TicketController.persist()` (user snapshots).
- `GroupController`: groups and derived lock (`TicketController.groupTicketsOf`).
- `TicketController`: tickets, shared id index, group-ticket creation; cascading deletions via `PassController.discardPassesOf(...)`.
- `PassController`: pass id index, purchases, refunds.
- `AttractionController`: attractions, entry/exit; persists passes after consumption.
- `InvoiceController`: acts as the Builder director.

### Exceptions
`business.exception`: `ParkException` (checked base) with `ValidationException` (bad input values), `NotFoundException`, `DuplicateException`, `AuthenticationException`, `OperationNotAllowedException` (business rules: locks, date rules, access denied, capacity) and `PersistenceException` (wraps `IOException`). `UserException` disappears. Model methods throw them (e.g. `Attraction.enter`), controllers propagate, and `CommandProcessor` is the single place that converts them into `ERROR: ...` lines; unexpected `RuntimeException`s are also caught there so processing never stops.

### Ticket access interfaces and generic attractions
- `Access` (general capability): `getId()`, `isValidOn(LocalDate)`, `getHolders()`, `getIndividualTickets()` (the ticket itself, or the composition).
- `IndividualAccess extends Access`: `getIndividualTicket()`. Implemented by `IndividualTicket`.
- `GroupAccess extends Access`: `getMemberTickets()`. Implemented by `GroupTicket`.
- `abstract class Attraction<T extends Access>` keeps `Class<T> accessType`; `enter(Ticket, boolean speed, LocalDate today)` is a template method: `accessType.isInstance` check (wrong type → `OperationNotAllowedException`), date check, then the abstract hook `passesToConsume(T access, boolean speed)` collects passes without mutating, then capacity check, then admits holders and consumes the passes. Nothing changes until all checks pass, which gives the atomic group behaviour.
- `GeneralAttraction extends Attraction<Access>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>` share a SpeedPass hook (one unconsumed SpeedPass per individual ticket); `SpecialAttraction extends Attraction<IndividualAccess>` uses `access.getIndividualTicket().getSpecialPass()` and rejects `speed`. The generic parameter therefore decides which tickets are accepted and which methods the hooks can call; INDIVIDUAL vs SPECIAL differ only in attraction behaviour, not in the ticket hierarchy.
- `AttractionType` enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) is the factory used by the controller and persistence.
- `Entry` (record) reports admitted users and consumed passes for output.

### Passes
`abstract class Pass` (id, `IndividualTicket ticket`, `BigDecimal price`, `boolean consumed`, `consume()`, `isRefundableOn(today)`), `SpeedPass` and `SpecialPass` with static `priceFor(ticket)` (20% / 100% of `ticket.getPrice()`, cents HALF_UP). The association is navigable both ways: `IndividualTicket` holds `List<SpeedPass>` (0..*) and `SpecialPass` (0..1), so attractions and invoices reach passes through the ticket; users and groups still hold no ticket references. A refunded Special Pass frees the slot (it no longer exists); a consumed one does not.

### Invoices with Builder
`InvoiceBuilder` (abstract, the common abstraction) has generic steps — `reset()`, `header(title, date)`, `recipient(lines)`, `section(title)`, `item(description, amount)`, `detail(text)` — and `build()` returns an immutable `Invoice`, dropping sections without lines and summing item amounts into the total. `UserInvoiceBuilder` adds `customer(User)`, `directTickets(List<IndividualTicket>)`, `speedPasses(List<SpeedPass>)`, `specialPasses(List<SpecialPass>)`; `GroupInvoiceBuilder` adds `group(Group)`, `groupTickets(List<GroupTicket>)` (one priced item per group ticket plus non-priced composition details). `InvoiceController` is the director: it gathers data and calls only the relevant steps. `Invoice.toText()` renders it.

### Help with injected rendering
Presentation builds a `CommandRegistry` of `CommandDefinition(keyword, syntaxes, description, handler)` registered by command groups (`UserCommands`, `SystemCommands`, `GroupCommands`, `TicketCommands`, `PassCommands`, `AttractionCommands`, `InvoiceCommands`, `GeneralCommands`). `HelpGenerator` receives the registry and a `Map<String, HelpRenderer>` (variant → renderer) by constructor; `HELP` uses the `""` variant (`SyntaxHelpRenderer`), `HELP EXTENDED` the `"EXTENDED"` variant (`ExtendedHelpRenderer`). New help formats are new renderers registered at wiring time; the command loop is unaffected. Because help is generated from the registry, syntax listings cannot drift from the commands.

### Passwords
`PasswordHasher` interface (`hash`, `matches`) in business; `BCryptPasswordHasher` uses `org.mindrot.jbcrypt.BCrypt` with a configurable log-rounds (10 in the application, 4 in tests for speed). `matches` returns false for values that are not BCrypt hashes (e.g. legacy plain text or former-user snapshots). `User` stores only the hash (`passwordHash`).

### Persistence formats
- `users.txt`: unchanged columns, password column holds the hash.
- `attractions.txt`: `id;name;capacity;type` (3 columns → GENERAL).
- `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;consumed` — ticket resolved through the ticket index (standalone and member tickets); unresolved lines skipped with a warning.
- groups/tickets unchanged.

### Build
Add `org.mindrot:jbcrypt:0.4`; `maven-shade-plugin` 3.6.0 bound to `package` with a `ManifestResourceTransformer` (`Main-Class`), producing `target/amusement-park.jar` as the shaded artifact (no dependency-reduced POM).

## Risks / Trade-offs

- [Large refactor touches every file] → Move packages first with tests green, then add features group by group, running the full suite each time.
- [Singletons with mutable state across tests] → `ApplicationContext.initialize` fully resets every controller; tests always initialise first.
- [BCrypt cost slows tests] → Injected hasher with low cost in tests.
- [Old `users.txt` with plain passwords cannot authenticate] → Accepted per assignment; documented in README.
- [Partial multi-file write on I/O failure] → Reported as `PersistenceException`; next successful save rewrites from memory.
