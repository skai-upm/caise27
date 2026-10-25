# Design

## Context

After F2 all classes live in `es.upm.parque`: `App` (I/O loop, `--fly`), `CommandProcessor` (registry of commands), `ParkController` (date, price and every cross-entity rule, returning `OK ...` strings), four `*Manager` classes holding in-memory lists, five text repositories plus `TextStore`/`TemporaryStorage`, and the domain model (`User`, `Group`, `Ticket`/`IndividualTicket`/`GroupTicket`, `Attraction`). jBCrypt 0.4 is in the local Maven repository. 122 tests exercise the commands through `ParkController.load(dir)` + `CommandProcessor`.

## Goals / Non-Goals

**Goals:**
- Physical layering with dependency direction presentation → business ← persistence (business owns repository interfaces).
- Singleton controllers wired by one composition root that also selects normal or `--fly` repository paths.
- Keep every E1/E2 command output identical so existing tests remain the regression suite.

**Non-Goals:**
- Persisting invoices; new query commands for passes; concurrency.

## Decisions

### Package layout
```
es.upm.parque                      App (entry point, args, --fly), ParkApplication (composition root)
es.upm.parque.presentation         CommandProcessor, CommandRegistry, CommandDefinition, ConsoleRunner
es.upm.parque.presentation.command SystemCommands, UserCommands, GroupCommands, TicketCommands,
                                   PassCommands, AttractionCommands, InvoiceCommands, HelpCommands
es.upm.parque.presentation.help    HelpGenerator, HelpFormatter (strategy), ConciseHelpFormatter, ExtendedHelpFormatter
es.upm.parque.business             ParkException, UserException
es.upm.parque.business.model       User, UserField, Group, Ticket, IndividualTicket, GroupTicket,
                                   AccessCredential, IndividualAccess, GroupAccess, Pass, SpeedPass, SpecialPass,
                                   Attraction<T>, GeneralAttraction, IndividualAttraction, GroupAttraction,
                                   SpecialAttraction, AttractionType
es.upm.parque.business.controller  ParkController (date, price), UserController, GroupController, TicketController,
                                   PassController, AttractionController, InvoiceController
es.upm.parque.business.invoice     Invoice, InvoiceSection, InvoiceLine, InvoiceBuilder (interface), StandardInvoiceBuilder
es.upm.parque.business.security    PasswordHasher (interface), BCryptPasswordHasher
es.upm.parque.business.validation  UserValidator
es.upm.parque.business.repository  UserRepository, GroupRepository, TicketRepository, PassRepository, AttractionRepository (interfaces)
es.upm.parque.persistence          TextStore, TemporaryStorage, Text*Repository implementations
```
`*Manager` classes become `*Controller`s. `ParkController` keeps only the system date and price (it remains the single holder of the date); rules move to the controller owning each entity. Controllers return domain objects; presentation command modules format the `OK ...` messages, keeping exact F1/F2 texts.

### Singleton + dependency injection
Each controller: private constructor, `private static final X INSTANCE`, `getInstance()`. A `configure(...)` method receives repositories and collaborating controllers; `load()` (re)reads state from its repository. `ParkApplication.start(dataDir, hasher)` creates the `Text*Repository` objects for `dataDir`, configures all controllers, loads them in order (users → groups → tickets → passes → attractions), registers command modules with the controllers they need, and returns the `CommandProcessor`. `App` only chooses `data/` or the `--fly` temp directory. Re-running `start` reconfigures the singletons, which is what tests and `--fly` use. Circular collaboration (users ↔ tickets ↔ groups) is fine because instances exist before `configure`.

Alternative: constructor injection without Singleton — rejected because C03 mandates Singleton controllers.

### Command registry and help
`CommandDefinition(name, syntax, description, parameterCount, action)`; the registry may hold several definitions per name with different parameter counts (optional `TIPO` and `SPEED`), chosen by the number of parameters. `HelpGenerator` is constructed with a `HelpFormatter` (strategy injected at startup); `HELP` uses a generator with `ConciseHelpFormatter`, `HELP EXTENDED` one with `ExtendedHelpFormatter`; both read `registry.definitions()`. New formats only need a new formatter.

### Passwords
`User` keeps `passwordHash`. `PasswordHasher` (business interface: `hash`, `matches`, `isHash`) with `BCryptPasswordHasher(logRounds)` (default 10; tests inject 4 for speed). `UserController` validates the plaintext format, then hashes; `authenticate` uses `matches`. On `load`, values that are not BCrypt hashes (`$2a$/$2b$/$2y$`, 60 chars) are hashed and the file rewritten.

### Passes
Abstract `Pass` (`id`, `price`, `consumed`, reference `ticket : IndividualTicket`) with `SpeedPass` and `SpecialPass`. Ids: `<ticketId>-SP<n>` (n = 1 + highest existing for that ticket) and `<ticketId>-SPECIAL`. Purchase requires an `IndividualTicket` dated ≥ today; refund requires unused and ticket date > today. A ticket "has" a Special Pass while one exists; after a refund a new one may be bought. `PassController` owns them and offers `deleteForTickets(...)` used by `TicketController` cascades (passes first, then tickets, then group ticket; all checks happen before any mutation).

### Access capabilities and generic attractions
`AccessCredential` (id, date, users, individual tickets) is implemented by `Ticket`; `IndividualAccess` (implemented by `IndividualTicket`) and `GroupAccess` (implemented by `GroupTicket`) extend it. `Attraction<T extends AccessCredential>` stores `Class<T> admitted`, exposes `T admit(AccessCredential)` (type check + cast, error if not admitted) and `int enter(T)`/`exit`. Subclasses: `GeneralAttraction extends Attraction<AccessCredential>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` overriding `allowsSpeed()` (false) and `requiresSpecialPass()` (true). `AttractionType` enum creates the right subclass. `AttractionController.enter` captures the wildcard in a generic helper: date check → `admit` → holders registered → passes to consume (SPEED: one unused SpeedPass per individual ticket; SPECIAL: unconsumed Special Pass) → capacity check inside `enter` → consume passes only after success.

### Invoices (Builder)
`InvoiceBuilder`: `start(recipient)`, `addIndividualTickets(list)`, `addPasses(list)`, `addGroupTickets(list)`, `build()`; `StandardInvoiceBuilder` creates sections only for non-empty lists and sums the total. `InvoiceController` is the director: for a user it adds direct tickets (individual tickets of the user not contained in any group ticket) and passes of all the user's individual tickets; for a group it adds its group tickets only. Presentation renders the `Invoice`.

### Persistence formats
- `passes.txt`: `id;SPEED|SPECIAL;ticketId;price;true|false`.
- `attractions.txt`: `id;name;capacity;TYPE` (3-field legacy lines → GENERAL).
- `users.txt`: same columns, password column holds the hash.

### Build
Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` (3.6.0, `ManifestResourceTransformer` with `es.upm.parque.App`, no dependency-reduced POM) so `target/parque.jar` stays executable.

### UML
Regenerate `uml/model.puml` from the new fields: packages as PlantUML packages, interfaces with `<|..` realisation, generic `Attraction<T extends AccessCredential>` and its bound subclasses, `Pass` hierarchy, controller associations and injected repository interfaces, with the F2 conventions.

## Risks / Trade-offs

- [Singletons hold state across tests] → `ParkApplication.start` reconfigures and reloads every controller; tests always start through it.
- [BCrypt slows tests] → injectable `PasswordHasher` cost; tests use 4 rounds.
- [Ten "mandatory commits" requested] → batch policy makes one commit per proposal; the labels go into the commit message (see proposal).
- [Large refactor may change outputs] → existing command-level tests are kept and must pass unchanged in expectations.
