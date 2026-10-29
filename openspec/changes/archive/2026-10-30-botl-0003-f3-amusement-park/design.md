# Design

## Context

Delivery 2 code: `command` (processor/runner), `model` (+`model.ticket`), `service` (`Park` singleton owning all state and rules, `UserService`, `OperationException`), `validation`, `persistence` (concrete file repositories, `ParkStorage`), `App`. Motivation/scope: proposal.md. Behaviour: specs/.

## Goals / Non-Goals

**Goals:** clean three-layer split; one exception hierarchy; singleton controllers wired by a hand-written composition root; replaceable repositories; generics that genuinely restrict attraction access; Builder that only builds relevant sections; keep the `Park` facade API so presentation and most tests stay stable.

**Non-Goals:** migrating old plain-text `users.txt`; persisting invoices; concurrency.

## Decisions

### Package layout
```
es.upm.amusementpark                 App (entry point), ParkInitializer (composition root / DI)
  .presentation                      CommandProcessor, CommandRunner, CommandSyntaxException, InvoicePrinter
  .presentation.help                 CommandCatalog, CommandInfo, HelpGenerator, HelpFormatter, SyntaxHelpFormatter, ExtendedHelpFormatter
  .business.exception                ParkException (checked root) + ValidationException, NotFoundException,
                                     DuplicateException, OperationNotAllowedException, AuthenticationException, PersistenceException
  .business.model                    User, UserField, Group
  .business.model.ticket             AccessTicket, IndividualAccess, GroupAccess (interfaces), Ticket, IndividualTicket, GroupTicket
  .business.model.pass               Pass, SpeedPass, SpecialPass, PassLookup
  .business.model.attraction         Attraction<T extends AccessTicket>, AttractionType, GeneralAttraction, IndividualAttraction,
                                     GroupAttraction, SpecialAttraction, EntryResult
  .business.invoice                  Invoice, InvoiceSection, InvoiceLine, InvoiceBuilder, UserInvoiceBuilder, GroupInvoiceBuilder, InvoiceDirector
  .business.controller               Park (facade), SystemController, UserController, GroupController, TicketController,
                                     PassController, AttractionController, InvoiceController
  .business.repository               UserRepository, GroupRepository, TicketRepository, AttractionRepository, PassRepository, Repositories
  .business.security                 PasswordHasher, BCryptPasswordHasher
  .business.validation               UserValidator, ValueParser
  .persistence                       FileUserRepository, FileGroupRepository, FileTicketRepository, FileAttractionRepository,
                                     FilePassRepository, StorageConfiguration (was ParkStorage), TextFiles
```
Dependency direction: presentation → business ← persistence (repository interfaces live in business; dependency inversion avoids a business→persistence cycle). Only the root package knows all three layers.

### Exceptions
`ParkException extends Exception` with subclasses per situation. Controllers/models throw them; repositories throw `IOException`, which controllers wrap into `PersistenceException`. `CommandProcessor.process` catches `ParkException` and `CommandSyntaxException` → `ERROR: msg`; it also catches `RuntimeException` → `ERROR: unexpected error: …` so processing never stops.

### Controllers, Singleton and injection
Every controller: `private static final X INSTANCE`, private constructor, `getInstance()`, and a `configure(...)` method taking its repository and collaborators. `ParkInitializer.initialize(Repositories, PasswordHasher, LocalDate today)` wires them in one place (setter injection resolves the user↔ticket↔group cycle), then calls `Park.load()`.

| Controller | State | Injected |
|---|---|---|
| SystemController | date, price | — |
| UserController | active users | UserRepository, PasswordHasher, TicketController, GroupController, AttractionController |
| GroupController | groups | GroupRepository, UserController, TicketController |
| TicketController | tickets | TicketRepository, SystemController, UserController, GroupController, PassController |
| PassController (implements PassLookup) | passes | PassRepository, SystemController, TicketController |
| AttractionController | attractions | AttractionRepository, SystemController, UserController, TicketController, PassController |
| InvoiceController | — | UserController, GroupController, TicketController, PassController |
| Park | — | all controllers |

`Park` keeps the Delivery 2 facade methods; after each mutating call it runs `saveAll()` (each controller saves its repository), so a command's effects are persisted together. *Alternative:* each controller saving itself — rejected, because user deletion touches users, groups and ticket records at once.

### Ticket access interfaces and generic attractions
```
interface AccessTicket { String getId(); boolean isValidOn(LocalDate); List<User> getUsers(); List<? extends IndividualAccess> getAccessTickets(); }
interface IndividualAccess extends AccessTicket { User getUser(); }
interface GroupAccess extends AccessTicket { List<? extends IndividualAccess> getComposition(); }
abstract class Ticket implements AccessTicket; IndividualTicket implements IndividualAccess; GroupTicket implements GroupAccess
abstract class Attraction<T extends AccessTicket> { Class<T> accessType; EntryResult admit(AccessTicket, boolean speed, PassLookup, LocalDate);
    protected EntryResult enter(T, …); protected List<Pass> selectPasses(T, boolean speed, PassLookup) }
GeneralAttraction extends Attraction<AccessTicket>; IndividualAttraction extends Attraction<IndividualAccess>;
GroupAttraction extends Attraction<GroupAccess>; SpecialAttraction extends Attraction<IndividualAccess>
```
`admit` checks `accessType.isInstance` (rejecting unsupported ticket kinds) and casts to `T`; `enter(T)` is a template method: validity → already-inside → capacity → `selectPasses` (may throw) → only then add occupants and consume passes, guaranteeing atomicity. `GroupAttraction.selectPasses` iterates `getComposition()` (a `GroupAccess` operation); `SpecialAttraction.selectPasses` rejects `SPEED` and uses `PassLookup.specialPassOf(IndividualAccess)`. INDIVIDUAL and SPECIAL share the same ticket bound; the Special Pass rule is the special attraction's own behaviour.

### Passes
`abstract Pass { id, IndividualTicket ticket, BigDecimal price, boolean consumed; consume(); isRefundable(today) }`, `SpeedPass.priceFor(ticket)` = 20 %, `SpecialPass.priceFor(ticket)` = ticket price. Prices are computed at purchase and stored. Passes reference their ticket; tickets do not reference passes (found by traversal in `PassController`, consistent with Delivery 2). `TicketController.deleteTicket/deleteGroupTicket` validate first, then call `PassController.removeFor(tickets)` and remove tickets — no partial state since all checks precede mutation.

### Invoices (Builder)
`InvoiceBuilder` interface: `reset()`, `buildHeader()`, `buildTicketItems()`, `buildPassItems()`, `buildTotal()`, `Invoice getResult()`. `UserInvoiceBuilder` and `GroupInvoiceBuilder` receive their recipient and the data they need (lists of tickets/passes) in their constructor; `GroupInvoiceBuilder.buildPassItems()` is a no-op. Sections are only added when they have lines. `InvoiceDirector.construct(builder)` calls the steps in order. `Invoice` is immutable (title, header lines, sections, total). `InvoicePrinter` (presentation) renders it.

### Help (strategy injection)
`CommandCatalog` holds `CommandInfo(syntax, description)` for every command. `HelpGenerator(CommandCatalog, HelpFormatter)` → `generate()`. `SyntaxHelpFormatter` prints syntax lines; `ExtendedHelpFormatter` prints syntax + description. `ParkInitializer`/`App` builds two generators and injects them into `CommandProcessor`.

### BCrypt
`PasswordHasher { String hash(String); boolean matches(String plain, String hash); }`; `BCryptPasswordHasher(int logRounds)` wraps `org.mindrot.jbcrypt.BCrypt`, treating malformed stored hashes as non-matching. Runtime cost 10; tests inject cost 4 for speed. `User` stores `passwordHash`; `users.txt` stores the hash. `maven-shade-plugin` produces the self-contained `nombre-aplicacion.jar` (main class in manifest).

### Persistence formats
- `attractions.txt`: `id;name;capacity;TYPE` (3-field lines → GENERAL).
- `passes.txt`: `SPEED;id;ticketId;price;consumed` / `SPECIAL;id;ticketId;price;consumed` (`consumed` = `true|false`); passes whose ticket is unknown are skipped with a warning.
- Other files unchanged except `users.txt` holding the hash.

## Risks / Trade-offs

- [Large mechanical package move risks breaking tests] → move with `git mv`, update imports, run the full suite before adding features.
- [Many singletons hinder isolation in tests] → `ParkInitializer.initialize` reconfigures and reloads every controller; tests always initialise.
- [BCrypt slows tests] → injectable cost factor.
- [Shaded JAR replaces the plain JAR] → same `finalName`, verified by running `java -jar` from a temp directory.

## Migration Plan

Delivery 2 data loads except passwords (plain text no longer verifies — no migration required by the source). Rollback: revert the commit.
