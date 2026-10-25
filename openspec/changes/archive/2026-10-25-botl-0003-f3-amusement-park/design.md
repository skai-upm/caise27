# Design

## Context

After Delivery 2 everything lives in `es.upm.parque`: `App` (args, `--fly`), `CommandProcessor` (command table, `OK`/`ERROR` output), `ParkController` (system date, price, cross-entity rules), entity managers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`), file repositories over a shared `TextFile` helper, and the domain (`User`, `Group`, `Ticket` → `IndividualTicket`/`GroupTicket`, `Attraction`). Rules throw `IllegalArgumentException`, which the presentation turns into `ERROR <reason>`. F3 imposes patterns explicitly (Builder, Singleton, DI, interfaces + bounded generics) and one external dependency (BCrypt). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Physical layering with business independent of persistence (repository interfaces in business, file implementations in persistence).
- Singleton controllers configured once at start-up by a composition root that also builds repositories for the chosen data directory.
- Patterns used where F3 asks for them, each with a functional role.

**Non-Goals:**
- Changing any E1/E2 command syntax, output, or data format other than the password hash and the new attraction type column.
- Persisting invoices, attraction occupancy, the system date or the price.

## Decisions

### Package layout
| Package | Contents |
|---|---|
| `es.upm.parque` | `App` (entry point, arguments, `--fly` directory and cleanup) and `ParkInitializer` (composition root). |
| `…presentation` | `CommandProcessor` (parsing, dispatch, `EXIT`, run loop), `Command`, `CommandRegistry`, command sets (`SystemCommands`, `UserCommands`, `GroupCommands`, `TicketCommands`, `PassCommands`, `AttractionCommands`, `InvoiceCommands`, `HelpCommands`). |
| `…presentation.help` | `HelpGenerator`, `HelpFormat` (strategy interface), `SyntaxHelpFormat`, `ExtendedHelpFormat`, `CommandInfo`. |
| `…business.model` | `User`, `UserField`, `Group`, `Admission` (interface), `Ticket`, `IndividualTicket`, `GroupTicket`, `Pass`, `SpeedPass`, `SpecialPass`, `Attraction<T>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, `EntryResult`. |
| `…business.controller` | Singleton controllers: `ParkController` (date, price), `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController`. |
| `…business.invoice` | `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` (interface), `StandardInvoiceBuilder`, `InvoiceDirector`. |
| `…business.repository` | Repository interfaces `UserRepository`, `GroupRepository`, `TicketRepository`, `PassRepository`, `AttractionRepository`. |
| `…business.security` | `PasswordHasher` (interface), `BCryptPasswordHasher`. |
| `…business.validation` | `UserValidator`, `Identifiers`. |
| `…persistence` | `TextFile`, `FileUserRepository`, `FileGroupRepository`, `FileTicketRepository`, `FilePassRepository`, `FileAttractionRepository`. |

Domain mutators that persistence needs while rebuilding objects (`Group.addMember`, `GroupTicket.addTicket`, pass attachment) become public; rule enforcement stays in controllers, which are the only API the presentation uses.
*Alternative:* repositories return raw records assembled by controllers — rejected: duplicates mapping code in every controller.

### Singletons and injection
Each controller: `private static final X INSTANCE = new X(); public static X getInstance()`, a private constructor, `configure(...)` receiving repositories and collaborator controllers, and `load()` that (re)reads its repository. `ParkInitializer.initialize(Path dataDir, LocalDate today, PasswordHasher hasher)` creates the `File*Repository` instances for the directory, calls `configure` on every controller (references only — order irrelevant), then `load()` in dependency order users → groups → tickets → passes → attractions, and returns a ready `CommandProcessor` factory. Re-initialising resets state, which also keeps tests independent. `--fly` only changes `dataDir`.
*Alternative:* lazy `getInstance(args)` — rejected: hides configuration and makes reconfiguration for `--fly`/tests awkward.

### Cross-controller rules
- `UserController.delete`: authenticate → `TicketController.hasCurrentOrFutureTicket` → `GroupController.removeUserEverywhere` → remove.
- `GroupController` mutations: `TicketController.isGroupLocked`.
- `TicketController.delete*`: validate everything first, then remove passes (via the ticket objects), internal tickets, the ticket; save tickets then `PassController.save()`. Validation-before-mutation gives "no partial deletions".
- `AttractionController.enter`: delegates to the attraction; if passes were consumed, `PassController.save()`.

### Access capability and bounded generics
`Admission` interface (`getId`, `getUsers`, `isValidOn(date)`, `getAccessTickets()` = individual tickets whose passes apply: itself for an individual ticket, its members' tickets for a group ticket). `Ticket implements Admission`.
`abstract class Attraction<T extends Admission>` keeps `Class<T> accepted`; `enter(Admission a, boolean speed, LocalDate today)` narrows with `accepted.isInstance/cast` (rejecting other kinds), checks date, presence, capacity, then asks the hook `passesToConsume(T ticket, boolean speed)`; only after every check passes are users added and passes consumed. Bindings: `GeneralAttraction extends Attraction<Ticket>`, `IndividualAttraction extends Attraction<IndividualTicket>`, `GroupAttraction extends Attraction<GroupTicket>`, `SpecialAttraction extends Attraction<IndividualTicket>` (overrides the hook: reject SPEED, require an unconsumed Special Pass). Default hook: with SPEED, one unused SpeedPass per access ticket or failure. `AttractionType` enum creates the right subclass (used by controller and repository).

### Passes
`abstract Pass(id, price, consumed)` with `SpeedPass` and `SpecialPass`; `IndividualTicket` owns `List<SpeedPass> speedPasses` and `SpecialPass specialPass` (composition). SpeedPass price = ticket price × 0.20 (scale 2, HALF_UP); Special = ticket price. Ids `<ticket>-SP<n>` (n = max existing + 1) and `<ticket>-SPECIAL`. Purchase requires the ticket date ≥ system date; refund requires unused and ticket date > system date. After a refund a Special Pass can be bought again (the "one per ticket" rule is enforced on the passes that exist; consumed passes are never deleted, so a used one blocks forever). File `passes.txt`: `id;SPEED|SPECIAL;ticketId;price;consumed`.

### Invoices (Builder)
`InvoiceBuilder` steps: `start(String recipient)`, `addSection(String title, List<InvoiceLine> lines)` (ignores empty sections), `build()` (computes the total). `StandardInvoiceBuilder` implements it. `InvoiceDirector` knows which steps apply: user → "Individual tickets" (direct tickets) + "Personal passes" (passes of all the user's individual tickets); group → "Group tickets". `InvoiceController` gets a `Supplier<InvoiceBuilder>` injected and a fresh builder per invoice. Output: `INVOICE <recipient>`, section headers, indented `id date/desc price` lines, `TOTAL: x`.

### Help (strategy by injection)
`Command` record: name, parameter syntax, description, min/max params, handler (`String run(String[])`, returns the output text). `CommandRegistry` keeps insertion order and exposes `List<CommandInfo>`. `HelpGenerator(HelpFormat)` renders a list of `CommandInfo`; `HelpCommands` receives two generators (syntax and extended) built in `ParkInitializer`. Optional parameters are shown in brackets: `ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]`.

### Passwords
`org.mindrot:jbcrypt:0.4` behind `PasswordHasher` (`hash`, `matches`, `isHash`). `User` stores `passwordHash`. Validation of the plaintext happens before hashing. `UserController.load()` hashes legacy plaintext values (anything not matching `$2a$/$2b$/$2y$` + 60 chars) and saves. Production cost 10; tests inject cost 4 for speed. `maven-shade-plugin` builds `target/parque.jar` with the dependency and the `es.upm.parque.App` manifest entry.

### Persistence compatibility
`TextFile.readRecords(file, min, max)` so `attractions.txt` accepts legacy 3-field lines (GENERAL). Other formats unchanged.

### UML
`uml/model.puml` rewritten from the final code with the same notation rules as Delivery 2, adding packages, generics (`Attraction<T extends Admission>`, bindings shown as `GeneralAttraction` → `Attraction<Ticket>` via inheritance labels), realisations of `Admission`, `InvoiceBuilder`, `HelpFormat`, `PasswordHasher` and repository interfaces. Validated with the PlantUML jar (`-checkonly`).

## Risks / Trade-offs

- [Singletons hold global state, complicating tests] → `ParkInitializer` reconfigures and reloads all controllers for each test; tests run sequentially (Surefire default).
- [BCrypt hashing is slow] → Injected hasher with low cost in tests.
- [Shaded JAR adds build time] → Acceptable; required for `java -jar` with a dependency.
- [Two files written on cascading deletes (tickets, passes) are not transactional] → Same risk profile as before; data sizes are small.
- [Proposal asks for ten "mandatory commits"] → The BOTL batch produces one commit per proposal; the F3 commit labels are recorded in the commit body and mapped to task groups below.

## Migration Plan

On first start, plaintext passwords in `users.txt` are hashed automatically; old `attractions.txt` lines load as GENERAL; `passes.txt` is created on first save. Rollback: revert the commit (hashed passwords would then not authenticate with older code — documented).
