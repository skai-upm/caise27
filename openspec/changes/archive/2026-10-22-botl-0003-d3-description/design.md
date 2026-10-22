# Design

## Context

After Delivery 2 the code lives in `es.upm.etsisi.park.{model,core,service,repository,persistence,cli,validation}` with a `Park` singleton that builds concrete repositories and services in `load(Path)`, a single `UserOperationException`, plain-text passwords and one `Attraction` class. See proposal.md for motivation and specs/ for behavior. Constraints: Java 21, Maven, executable JAR, no DI frameworks, no serialization/DB, only jBCrypt as extra runtime library; everything from Deliveries 1–2 keeps working (including `--fly`).

## Goals / Non-Goals

**Goals:**
- Visible layering: `presentation` → `business` → `persistence`, with the composition root in `App`.
- Singleton controllers configured by injection; repositories as interfaces whose file implementations are created once per execution from a `DataPaths`.
- Typed attractions whose generic parameter actually restricts the tickets they handle.
- Atomic entry (validate everything, then mutate) and atomic pass purchases/cascades.

**Non-Goals:**
- Migrating plain-text `users.txt` files from earlier versions (explicitly not required).
- Persisting invoices; reproducing the course's `[F3-Cxx]` commit roadmap.

## Decisions

**Packages.**
- `presentation`: `CommandProcessor`, `CommandRunner`, `InvoicePrinter`, `presentation.help` (`HelpFormatter`, `SimpleHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`, `CommandInfo`).
- `business.model` (+ `business.model.access`, `business.model.attraction`), `business.controller`, `business.invoice`, `business.exception`, `business.validation`, `business.security`.
- `persistence` (repository interfaces, `Repositories`, `DataPaths`) and `persistence.file` (stores and file-backed repositories).
- `App` (root package) is the composition root: parses arguments, creates repositories, configures controllers, builds the presentation objects.
Alternative: layer-by-feature packages — rejected, the assignment asks for layer packages.

**Exceptions.** `ParkException` (unchecked) with `ValidationException` (bad input), `NotFoundException`, `DuplicateException`, `RuleViolationException` (business rules: locks, dates, capacity, passes, access type), `AuthenticationException`, `PersistenceException` (wraps I/O). Controllers and domain objects throw; only `CommandProcessor` catches and prints `ERROR: <message>`; `App` handles `PersistenceException` at startup.

**Controllers and DI.** `SystemController`, `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController` are singletons (`getInstance()`), each with a `configure(...)` method receiving repositories, `SystemState`, collaborators (`PasswordHasher`, `PassLookup`) — setter injection is the only option compatible with `getInstance()`. `Park` stays the central singleton (Delivery 2 requirement): `configure(Repositories, PasswordHasher)` creates the execution's `SystemState` and wires every controller; it exposes the controllers and repositories. Repositories are never singletons: `FileRepositories.open(DataPaths)` creates a new set per call, so `--fly` only passes another directory. Collaboration example: `AttractionController` receives `PassController` as its `PassLookup`.

**Repositories with generics.** `Repository<T>` (`findById`, `findAll`, `add`, `remove`, `save`) is extended by `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` (`Repository<Attraction<?>>`), `PassRepository` (adds `addAll`/`removeAll` for atomic multi-pass changes). `ListRepository<T>` is the generic in-memory base of all `File*Repository` classes, which delegate to the existing line-based stores.

**Passwords.** `PasswordHasher` interface (`hash`, `matches`) with `BCryptPasswordHasher(int logRounds)` (default 10; tests inject 4 for speed). `User` stores `passwordHash`; `matches` returns false for values that are not valid BCrypt hashes (old plain-text data).

**Passes.** Abstract `Pass` (id, `IndividualTicket`, stored `price`, `consumed`) with `SpeedPass` (price = 20 % of ticket price, scale 2 HALF_UP) and `SpecialPass` (price = ticket price). Direction pass → ticket only; `PassRepository.findByTicket` answers the reverse. Pass id space is shared by both pass kinds. A ticket may hold at most one Special Pass at a time (a refunded one is removed from the state, so a new one may be bought; a consumed one blocks forever because it stays stored). File: `passes.txt` with `SPEED|SPECIAL;id;ticketId;price;consumed`, loaded after tickets.

**Access interfaces and generic attractions.**
- `Access` (id, date, users, admitted individual tickets, `isValidOn`), `GeneralAccess extends Access`, `IndividualAccess extends GeneralAccess` (`getTicket()`), `GroupAccess extends GeneralAccess` (`getTickets()`); `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
- `abstract class Attraction<T extends Access>` keeps id/name/capacity/occupants, `Class<T> accessType`, `Optional<T> admit(Ticket)`, and `List<Pass> enter(T access, boolean speed, PassLookup passes)`: computes required passes via `requiredPasses(T, speed, passes)`, checks capacity, then adds occupants and consumes passes (nothing mutates before all checks pass).
- `GeneralAttraction extends Attraction<GeneralAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (overrides `requiredPasses`: rejects SPEED, requires the unconsumed Special Pass of `access.getTicket()`). INDIVIDUAL and SPECIAL share the ticket capability; the difference is the attraction's behavior.
- `AttractionType` enum creates the right subclass; `attractions.txt` gains a 4th column (missing → GENERAL).

**Invoices (Builder).** `Invoice` (title, recipient lines, sections, total) with package-private constructor; `InvoiceSection`/`InvoiceLine` records (lines may carry detail rows without amounts, e.g. group-ticket composition). `InvoiceBuilder` interface (`build()`), generic `AbstractInvoiceBuilder<B>` (title, recipient lines, `section(...)` that skips empty lists, total = sum of line amounts), `UserInvoiceBuilder` (`forUser`, `withIndividualTickets`, `withSpeedPasses`, `withSpecialPasses`) and `GroupInvoiceBuilder` (`forGroup`, `withGroupTickets`). `InvoiceController` acts as director, reading repositories; `InvoicePrinter` renders text.

**Help.** `CommandInfo(syntaxes, description)` is attached to each registered command. `HelpGenerator(HelpFormatter)` renders a list of `CommandInfo`; `App` injects a `LinkedHashMap<String, HelpGenerator>` (`""` → simple, `"EXTENDED"` → extended) and the processor registers one `HELP <variant>` command per entry, so new help styles need no change in command processing. Commands may have several arities (`ATTRACTION CREATE` 3/4, `ATTRACTION ENTER` 2/3).

**Build.** Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (main class in manifest) so `target/amusement-park.jar` contains jBCrypt and runs with `java -jar`.

## Risks / Trade-offs

- [Big-bang package move breaks history readability] → One change, tests moved alongside; behavior covered by the existing Delivery 1–2 tests before new features are added.
- [Setter-injected singletons can be used unconfigured] → Accessors throw `IllegalStateException` if `configure` was not called.
- [BCrypt slows tests] → Injected cost factor (4 in tests).
- [Special Pass "at most one" after a refund is interpreted as "at most one existing"] → Documented; consumed passes are never deleted, so the strict rule still holds for consumed passes.
- [Assignment examples are line-wrapped (`Clave1` / `!`)] → Intended passwords are `Clave1!`, `Otra2#`, `Laura3$`.
