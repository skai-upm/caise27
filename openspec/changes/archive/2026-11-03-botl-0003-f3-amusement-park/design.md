# Design

## Context

Delivery 2 lives in the flat package `es.upm.etsisi.park`: `App` (args, `--fly`, wiring), `CommandRunner`, `CommandProcessor` (registry of fixed-arity commands, `HELP` derived from it), `Park` (state singleton with `start(ParkStorage, LocalDate)` and `commit(undo)`), services (`UserService`, `SystemService`, `GroupService`, `TicketService`, `AttractionService`) built per processor, model (`User`, `UserField`, `UserValidator`, `Group`, `Ticket`/`IndividualTicket`/`GroupTicket`, `Attraction`), `ValidationException`, and persistence (`TextFileRepository` + four repositories, `ParkStorage`). Runtime has no dependencies; the JAR is a plain `maven-jar-plugin` artifact. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Clear layers with one-way dependencies: presentation → business ← persistence (persistence implements a business interface), wired by `App`.
- Keep every Delivery 1/2 command and output unchanged except where the source changes them.
- Every new multi-object operation (pass packages, cascades, SPEED group entry) validates fully before mutating and is undone if saving fails.

**Non-Goals:**
- Migrating plaintext `users.txt` files (source §8 says not required).
- Persisting invoices or occupancy.

## Decisions

### D1. Packages
- `es.upm.etsisi.park` — `App` only (entry point and composition root: argument handling, `--fly`, object graph).
- `…presentation` — `CommandRunner`, `CommandProcessor`, `HelpGenerator`, `HelpFormatter` + `SyntaxHelpFormatter` / `ExtendedHelpFormatter`, `CommandInfo`.
- `…business` — model, `Park`, controllers, access interfaces, attraction types, passes, `ParkRepository` (the persistence port) and the exception hierarchy.
- `…business.invoice` — `Invoice`, `InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`.
- `…persistence` — `TextFileRepository`, `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, `PassRepository`, `TextParkRepository implements ParkRepository`.

The model and controllers share one business package so model mutators can stay package-private; mutators that persistence needs while loading (e.g. `Group.addMember`, `Pass.markConsumed`) become public.

### D2. Exceptions
`ParkException` (checked, abstract) with `ValidationException` (malformed or invalid input, duplicates), `NotFoundException` (unknown user/group/ticket/attraction/pass), `OperationNotAllowedException` (business rules: locks, dates, capacity, attraction type, passes) and `PersistenceException` (save failure, raised by `Park.commit` after undo). `CommandProcessor` catches `ParkException` and prints `ERROR: <message>`; messages are unchanged from Delivery 2.

### D3. Singleton controllers and injection
Controllers: `UserController`, `SystemController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController`. Each has a private constructor, a `private static final` instance, `getInstance()`, and a `configure(...)` method receiving its collaborators (`Park`, and `PassController` for `TicketController` — cascade — and `AttractionController` — consumption; `InvoiceController` receives `Park` and `PassController`). `Park` keeps its Delivery 2 singleton role as state holder and receives the `ParkRepository` in `start(ParkRepository, LocalDate)`. `App.configure(ParkRepository, LocalDate)` is the single wiring point used by `main` and by tests; `App` builds a `TextParkRepository` for the normal or `--fly` directory, so only the path differs. Tests can inject another `ParkRepository` (e.g. one whose save fails). No DI framework.

Alternative: constructor injection into new controller objects — rejected because the source asks for Singleton controllers; `configure` is the hand-written injection point.

### D4. Command registry and help
A registry entry is (name, accepted parameter counts → handler, syntax lines, purpose). `ATTRACTION CREATE` accepts 3 or 4 parameters, `ATTRACTION ENTER` 2 or 3 (the 3rd must be `SPEED`), `INVOICE CREATE` 2 (first `USER` or `GROUP`; two syntax lines). `HELP` and `HELP EXTENDED` are registry entries. `HelpGenerator` is constructed with a `HelpFormatter` (strategy) and formats the registry's `CommandInfo` list; `App` injects one generator per format into `CommandProcessor`. New formats only need a new `HelpFormatter`.

Syntax lines follow the source listings in order (Delivery 1, Delivery 2, then Delivery 3 additions: typed `ATTRACTION CREATE` next to the old form, `ATTRACTION ENTER …;SPEED` next to the plain form, passes, invoices, `HELP`, `HELP EXTENDED`, `EXIT`).

### D5. BCrypt
Dependency `org.mindrot:jbcrypt:0.4`, packaged into the executable JAR by `maven-shade-plugin` 3.6.0 (manifest `Main-Class` set by the shade `ManifestResourceTransformer`; the jar keeps the name `amusement-park.jar`). `business.PasswordHasher` wraps `BCrypt.hashpw(pw, BCrypt.gensalt())` and `BCrypt.checkpw`; `User` stores `passwordHash`. `checkPassword` returns `false` (no exception) for values that are not BCrypt hashes, which covers legacy plaintext files (**flagged**: such users cannot authenticate until recreated). The password is validated with `UserValidator` before hashing.

### D6. Passes
- `abstract Pass` (`id`, `IndividualTicket ticket`, `BigDecimal price`, `boolean consumed`); `SpeedPass` (price = ticket price × 0.20, scale 2 `HALF_UP`) and `SpecialPass` (price = ticket price). Direction Pass → IndividualTicket; tickets do not hold passes (lookups traverse the park's passes, like tickets for users).
- `Park` holds passes in one `LinkedHashMap<String, Pass>` (pass identifier space shared by both pass types, separate from tickets).
- `PassController`: buy SpeedPasses (quantity exactly `1` or `10`, ids `<p>:1..n`, all ids checked first), buy Special Pass (one per ticket while any Special Pass for it is stored), refunds, `passesOf(ticket)`, `unusedSpeedPass(ticket)`, `specialPassOf(ticket)`, and `removePassesOf(tickets)` for cascades.
- Target ticket must be an individual ticket (standalone or composed) dated on or after today; a group ticket id is rejected.
- **Flagged:** "at most one Special Pass throughout its entire existence" is enforced against stored passes; after a refund (which removes the pass) a new Special Pass can be bought. Pass purchase for a deleted user's historical ticket is impossible anyway because the date must not be past.

### D7. Attraction types with interfaces and bounded generics
- Access interfaces: `GeneralAccess` (`List<IndividualTicket> entryTickets()`), `IndividualAccess extends GeneralAccess` (`IndividualTicket individualTicket()`), `GroupAccess extends GeneralAccess` (`List<IndividualTicket> getComposition()`). `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
- `abstract class Attraction<T extends GeneralAccess>` keeps id, name, capacity, occupants and a `Class<T>` token. `enter(Ticket, boolean speed, LocalDate, PassController)` narrows the ticket to `T` (rejecting other kinds), checks date, capacity and duplicates, then calls the typed hook `List<Pass> passesToConsume(T access, boolean speed, PassController)`; only after all checks it adds occupants and marks passes consumed. Default hook: SPEED → one unused SpeedPass per `entryTickets()` element (error naming the ticket without one).
- `GeneralAttraction extends Attraction<GeneralAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` overriding the hook: rejects SPEED, requires `specialPassOf(access.individualTicket())` unconsumed. `AttractionType` enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) creates the matching subclass.
- Entry now changes persisted state (consumed passes), so it commits; undo restores occupancy and consumed flags.
- Output: `ATTRACTION ENTERED: <n> USER(S)` plus `. SPEEDPASS CONSUMED: <ids>` or `. SPECIAL PASS CONSUMED: <id>`.
- **Flagged:** ATTRACTION EXIT/LIST/STATUS are type-independent; STATUS adds a `TYPE:` line.

### D8. Invoices (Builder)
- `Invoice` is immutable: title, header lines, ordered sections (title + lines with optional amount), total.
- `InvoiceBuilder` (common abstraction): `title(String)`, `headerLine(String)`, `section(String)`, `item(String, BigDecimal)` (counted in the total), `detail(String)` (informative, not counted), `build()`. `AbstractInvoiceBuilder` implements the steps and drops sections that received no lines.
- `UserInvoiceBuilder` adds `forUser(User)`, `directTickets(List)`, `speedPasses(List)`, `specialPasses(List)`; `GroupInvoiceBuilder` adds `forGroup(Group)`, `groupTickets(List)` (each group ticket as an item with its composition as details).
- `InvoiceController` is the director: it gathers data from `Park`/`PassController` and calls the builder steps. User invoice: standalone tickets of the user (all dates), SpeedPasses and Special Passes of any individual ticket of the user. Group invoice: group tickets whose group reference is the group.
- `INVOICE CREATE` prints `Invoice.toText()`.

### D9. Persistence
- `attractions.txt`: `id;name;capacity;type`; 3-field lines load as GENERAL.
- `passes.txt`: `S;id;ticketId;price;consumed` (SpeedPass) and `P;…` (Special Pass); the ticket is resolved with `Park.findTicket` among individual tickets; records with unknown tickets are skipped with a warning.
- `users.txt`: unchanged layout; field 5 is the hash.
- Load order: users → attractions → groups → tickets → passes. Save writes all five files (each via temp file + move).
- Cascades (`TICKET DELETE`, `GROUPTICKET DELETE`): remove passes and ticket(s) in memory, then one commit; undo re-adds them.

### D10. UML
`uml/model.puml` is rewritten with the three layer packages, the access interfaces, generic attractions (`Attraction<T extends GeneralAccess>` with bound shown on each subclass), passes with `IndividualTicket "1" <-- "0..*" SpeedPass` and `"0..1" SpecialPass`, invoice builders, singleton controllers (`<<singleton>>`), `ParkRepository` / `TextParkRepository` and injected collaborations. `UmlModelTest` (updated for packages) keeps checking classes and field-based associations against the code; PlantUML `-syntax` check from the cached jar.

### D11. Milestone commits
As in Deliveries 1 and 2, the ten `[F3-Cxx]` messages are not produced as separate commits because the batch harness owns Git history and per-phase SDDX git metrics; tasks remain ordered by checkpoint. **Flagged conflict.**

## Risks / Trade-offs

- [BCrypt cost slows tests] → Default cost (10) kept for production; acceptable test time; no test-only switch in production code.
- [Shaded JAR] → Larger artifact; `java -jar target/amusement-park.jar` unchanged for users.
- [Singleton controllers with `configure`] → Re-configured on every start (needed for tests and for `--fly`); `App.configure` is the only place that calls it.
- [Large package move] → Done first (C01) with the whole Delivery 2 suite as regression net before adding features.

## Migration Plan

Delivery 2 data files load unchanged except passwords (plaintext records fail authentication, as allowed by the source). Rollback: revert the commit.
