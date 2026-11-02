# Design

## Context

At the end of Delivery 2 all code lives in `es.upm.etsisi.park`:

- `Park` is a singleton that owns all state, every rule, and an undo-on-save-failure `commit`.
- The ticket hierarchy is `Ticket` → `IndividualTicket` / `GroupTicket`.
- `Attraction` is a single concrete class.
- Text repositories are aggregated by `ParkRepository(dir)`.
- `CommandProcessor` is a large `switch`.
- `App` handles `--fly` through `TemporaryStorage`.

See proposal.md for scope and specs/ for behavior.

## Goals / Non-Goals

**Goals:**
- Layering with dependency inversion. The business layer owns the repository abstraction, and persistence implements it. Presentation only talks to controllers.
- Keep all Delivery 1 and 2 outputs and rules unchanged, except the documented modifications: hashed passwords and attraction types.
- Make every multi-object mutation all-or-nothing, using the existing undo-on-save-failure pattern. This covers cascades and SPEED group entry.

**Non-Goals:**
- Migrating plaintext `users.txt` from earlier deliveries. The source says it is not required. A plaintext stored value simply never authenticates.
- Persisting invoices or occupants.
- A DI container or reflection-based wiring.

## Decisions

1. **Packages.**
   - `es.upm.etsisi.park`: `App` (entry point) and `ParkApplication` (composition root).
   - `…presentation`: `CommandProcessor`, `CommandDefinition` (registry entry: keyword, syntax lines, description, accepted parameter counts, handler), `HelpGenerator`, the `HelpFormatter` strategy with `SyntaxHelpFormatter` and `ExtendedHelpFormatter`.
   - `…business`: model, controllers, `Park`, validators, `Money`, exceptions, the `ParkRepository` interface plus `ParkData`, and `PasswordHasher` with `BCryptPasswordHasher`.
   - `…business.invoice`: the Builder classes.
   - `…persistence`: `TextParkRepository implements ParkRepository`, the per-entity file stores, `TextFile` and `TemporaryStorage`.

   `business` never imports `persistence`. *Alternative:* sub-packages per entity. Rejected because package-private mutators (for example `User.setEmail`) must stay hidden from presentation but reachable by controllers.
2. **Exceptions.** `ParkException` is the base class. It has `UserException`, `NotFoundException`, `ConflictException` and `RuleViolationException` subclasses, plus `PersistenceException`, which is also thrown by persistence implementations through the business interface. `CommandProcessor` is the only handler and prints `ERROR: <message>`.
3. **Controllers.** `Park` (state plus system date and price) and six controllers — `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController` and `InvoiceController` — are all singletons with a private constructor, a static `getInstance()`, and `configure(...)` for injection. `ParkApplication.start(ParkRepository, LocalDate, PasswordHasher)` wires them:
   - `Park.start(repository, today)`.
   - `UserController.configure(park, hasher)`.
   - `PassController.configure(park)`.
   - `TicketController.configure(park, passController)`. This is a controller collaboration used for cascade deletes.
   - `AttractionController.configure(park, passController)`.
   - `GroupController.configure(park)` and `InvoiceController.configure(park)`.

   Repositories are plain objects created by `ParkApplication`'s caller (`App`) with the chosen directory. Only that directory differs between normal and `--fly` modes.
4. **BCrypt.** `PasswordHasher` has `hash` and `matches`. `BCryptPasswordHasher(int logRounds)` wraps `org.mindrot.jbcrypt.BCrypt`: production uses 10 rounds and tests use 4, which is injected. `matches` returns `false` for values that are not BCrypt hashes, including legacy plaintext. `User` keeps the `password` column, which now holds the hash, and `checkPassword` moves to `UserController`. The `maven-shade-plugin` bundles jBCrypt into `target/nombre-aplicacion.jar`. `finalName` and `mainClass` are unchanged.
5. **Access interfaces and generics.**
   - `AccessTicket` (with `isValidOn` and `getEntryTickets()`, returning the individual tickets that enter) is implemented by `Ticket`.
   - `IndividualAccess extends AccessTicket` is implemented by `IndividualTicket`.
   - `GroupAccess extends AccessTicket` is implemented by `GroupTicket`.
   - `abstract class Attraction<T extends AccessTicket>` holds a `Class<T>` token. `cast(AccessTicket)` rejects non-accepted kinds, and `admit(T, List<User>)` is the typed admission.
   - Subclasses:
     - `GeneralAttraction extends Attraction<AccessTicket>`.
     - `IndividualAttraction extends Attraction<IndividualAccess>`.
     - `GroupAttraction extends Attraction<GroupAccess>`.
     - `SpecialAttraction extends Attraction<IndividualAccess>`. It overrides `acceptsSpeedPass()` to return `false` and `requiresSpecialPass()` to return `true`.
   - `AttractionType` creates the right subclass.
   - `AttractionController.enter` captures `T` via a generic helper `<T extends AccessTicket> void admit(Attraction<T> a, AccessTicket t)`, so the type parameter really governs admission.
6. **Passes.** `abstract Pass` holds `id`, `ticket` (an `IndividualTicket`), `price` and `consumed`. `SpeedPass` and `SpecialPass` extend it.
   - Passes reference tickets, and tickets do not reference passes. Lookups traverse `Park.passes`, a single `LinkedHashMap` that is also the shared pass identifier space.
   - Prices are computed once at purchase from `ticket.getPrice()` and stored: SpeedPass = 20%, Special Pass = 100%.
   - Purchases check every generated id `<purchase>:<n>` before creating any pass.
7. **Entry algorithm** (`AttractionController.enter(attraction, ticket, speed)`).
   - Checks, in order: attraction exists, ticket exists, ticket is valid today, the attraction accepts the ticket kind, SPEED is allowed if requested, there is capacity, and no holder is already inside.
   - Then it picks the passes to consume: one unconsumed SpeedPass per entry ticket (lowest id first), or the ticket's unconsumed Special Pass at a SPECIAL attraction. Any missing pass rejects the entry before anything changes.
   - Then it admits the holders, marks the passes consumed, and persists. If the save fails, it undoes both.
   - Output: `ATTRACTION ENTERED <n> USER(S)`, plus `; SPEEDPASS CONSUMED: <ids>` or `; SPECIALPASS CONSUMED: <id>`.
8. **Invoices.**
   - `InvoiceBuilder` is the interface, with steps `buildHeader()`, `buildRecipient()`, `buildPurchases()` and `getInvoice()`.
   - `AbstractInvoiceBuilder` holds the shared part-assembly: sections and totals.
   - `UserInvoiceBuilder` has sections *Tickets*, *SpeedPasses* and *Special Passes*. `GroupInvoiceBuilder` has the section *Group tickets*, with a non-charged composition detail line for each constituent.
   - `InvoiceDirector.construct(builder)` runs the steps. Each section is added only if it is non-empty.
   - `Invoice` holds `title`, recipient lines and `List<InvoiceSection>` (made of `InvoiceLine`, which is either `ChargeLine(desc, amount)` or `DetailLine(text)`). The total is the sum of `ChargeLine` amounts. `render()` returns the text. There are no optional-parameter constructors.
   - Invoices include historical items, because they are fiscal records.
9. **Persistence.**
   - New `passes.txt`, one line per pass: `S|P;id;ticketId;price;consumed`. The ticket can be a standalone ticket or a constituent.
   - `attractions.txt` becomes `id;name;capacity;type`. A 3-field line loads as GENERAL.
   - `ParkData` is a record of lists: users, groups, tickets, passes, attractions.
   - `TextParkRepository.save` writes all five files with the existing temp-then-move routine.
10. **Command registry and help.**
    - `CommandProcessor` builds an ordered `Map<String, CommandDefinition>` keyed by the first `;`-token, for example `HELP EXTENDED` or `ATTRACTION ENTER`, and validates parameter counts against each definition's allowed counts.
    - `HelpGenerator(HelpFormatter)` renders the registry. Two generators are injected: syntax-only for HELP and syntax plus description for HELP EXTENDED.
    - The HELP syntax lines are exactly those of the Delivery 2 and Delivery 3 source §15. The Delivery 2 `ATTRACTION CREATE;<identifier>;<name>;<capacity>` line is kept for compatibility.
11. **ATTRACTION STATUS** gains a `Type:` line. All required fields are still present.

## Risks / Trade-offs

- **Mandatory milestone Git history: 10 commits with exact messages.** This conflicts with the batch harness, which owns Git history and creates one commit per proposal. The `[F3-C01] …` to `[F3-C10] …` commits are therefore not made separately. tasks.md keeps checkpoint order with verification at each step. Flagged, not silently resolved.
- **"At most one Special Pass throughout its entire existence"** is ambiguous once a pass has been refunded, because refunded passes leave the persisted state. The interpretation is: at most one *existing* Special Pass, consumed or not. After a valid refund another may be bought. Consumption never allows a second one.
- **Using a group constituent ticket (`TG01:<dni>`) individually**, which is allowed since Delivery 2, counts as individual access. INDIVIDUAL and SPECIAL attractions therefore accept it, and the passes on that constituent apply.
- **Which SpeedPass is consumed** when several are unused: the one with the lowest identifier, in insertion order. The source allows any choice, provided the output names it.
- **Legacy plaintext `users.txt`** loads, but those users cannot authenticate. This is acceptable per the source and is covered by a test.
- **BCrypt cost.** 10 rounds take about 80 ms per hash. Tests inject 4 rounds to stay fast, and production keeps 10.
- **Shaded JAR.** The shade plugin replaces the main artifact. `finalName` keeps the path `target/nombre-aplicacion.jar`, and the dependency-reduced POM is gitignored (already listed in `.gitignore`).
- **Large refactor of package moves.** Mitigation: the Delivery 1 and 2 test suites are ported first and must pass before new features are added (task group 1).

## Migration Plan

Run once with the new JAR. Old 3-field attraction lines are read as GENERAL. Plaintext passwords must be re-registered, since migration is explicitly not required. Rollback means reverting the commit.
