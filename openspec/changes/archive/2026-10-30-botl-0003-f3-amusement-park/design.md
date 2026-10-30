# Design

## Context

All Delivery 2 code sits in `es.upm.amusementpark`: a `Park` singleton that builds and owns the file repositories (`open(dir)`), stateless services that call `Park.getInstance()`, `IllegalArgumentException` for every rejection, plain-text passwords, and a non-generic `Attraction`. Motivation: proposal.md. Behaviour: specs.

## Goals / Non-Goals

**Goals:**
- Package layering that makes dependencies visible: presentation → business → persistence interfaces, with the file implementations only known to the composition root.
- Controllers as Singletons configured by injection; repositories as replaceable instances.
- Compile-time restriction of ticket types per attraction via bounded generics.

**Non-Goals:**
- Migrating Delivery 2 plain-text users files.
- Persisting invoices or attraction occupants.

## Decisions

1. **Packages.**
   - `es.upm.amusementpark`: `App` (entry point, argument handling) and `ApplicationInitializer` (composition root).
   - `presentation`: `CommandProcessor`, `CommandRunner`, `HelpGenerator`, `HelpFormatter` (strategy) with `SyntaxHelpFormatter` / `ExtendedHelpFormatter`, `CommandCatalog` / `CommandHelp`, `InvoiceRenderer`.
   - `business.model`: `User`, `UserField`, `UserValidator`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, access interfaces, `Pass`, `SpeedPass`, `SpecialPass`, `PassWallet`, `Attraction<T>` and its four subclasses, `AttractionType`, `ParkFormat`.
   - `business.controller`: `Park`, `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController`, `SystemController`.
   - `business.invoice`: `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector`.
   - `business.security`: `PasswordHasher`, `BCryptPasswordHasher`.
   - `business.exception`: `ParkException` (unchecked root) and subtypes.
   - `persistence`: repository interfaces + `PersistenceException`; `persistence.file`: `File*Repository`, `FileRepositories` (creates and loads them for a directory), `TemporaryDataDirectory`.

2. **Singletons + setter injection.** Each controller has a private constructor, `getInstance()` and a `configure(...)` method receiving its repositories and collaborators (e.g. `TicketController.configure(park, tickets, users, groups, passController)`). `ApplicationInitializer.initialize(dataDir, hasher)` builds `FileRepositories` for that directory, configures every controller, and returns a `CommandProcessor` built with the controllers and help generators. Repositories are plain objects created per initialisation. *Alternative:* constructor injection with non-singleton controllers. Rejected because the source requires Singleton controllers.

3. **Exceptions.** Domain and controllers throw `InvalidDataException`, `NotFoundException`, `DuplicateException`, `AuthenticationException` or `OperationNotAllowedException` (all extend `ParkException extends RuntimeException`). Repositories throw `PersistenceException`. `CommandProcessor` is the only place that catches them and turns them into `ERROR:` lines. Unchecked exceptions were chosen so that domain methods stay readable; the hierarchy still carries meaning, and tests assert on specific types.

4. **Access capabilities and generics.**
   - `AccessTicket` (general capability: id, validity date, holders, admitted individual tickets).
   - `IndividualAccess extends AccessTicket` (`getIndividualTicket()`) is implemented by `IndividualTicket`; `GroupAccess extends AccessTicket` (`getComposition()`) is implemented by `GroupTicket`. `Ticket` implements `AccessTicket`.
   - `abstract class Attraction<T extends AccessTicket>` stores `Class<T>` and exposes `accepts(AccessTicket)`, `admit(AccessTicket)` (checked cast to `T`) and `enter(T ticket, boolean speed, PassWallet wallet)`. `enter` checks capacity and duplicates, asks the hook `passesToConsume(T, speed, wallet)` for the passes to consume (default: one available SpeedPass per admitted ticket when `speed`), and only then mutates occupants and passes.
   - `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` overriding the hook (rejects SPEED, requires the unconsumed Special Pass of `getIndividualTicket()`).
   - `AttractionType` enum creates the right subclass.

5. **Passes.** `abstract Pass` (id, `IndividualTicket ticket`, `BigDecimal price`, `consumed`); `SpeedPass` (price = 20 % of ticket price) and `SpecialPass` (price = ticket price), both computed once at purchase. Passes reference their ticket; tickets do not reference passes, and cardinalities are enforced by `PassController`. `PassController` implements `PassWallet` (oldest unconsumed SpeedPass, the Special Pass of a ticket).

6. **Cascade deletes.** `TicketController` validates first, then asks `PassController.removePassesOf(tickets)` and removes the ticket(s); both repositories are saved, so a failed validation leaves everything intact.

7. **Invoices (Builder).** `InvoiceBuilder` declares the steps `reset`, `buildHeader`, `buildRecipient`, `buildItems`, `buildTotal`, `getResult`. `AbstractInvoiceBuilder` holds the shared parts and adds a section only when it has lines. `UserInvoiceBuilder` adds direct tickets, SpeedPasses and Special Passes; `GroupInvoiceBuilder` adds group tickets with composition details that are not counted. `InvoiceDirector` runs the steps in order. `Invoice` is immutable; `InvoiceRenderer` (presentation) turns it into text.

8. **Help.** `CommandCatalog` lists `CommandHelp(syntax, description)` for every command. `HelpGenerator` receives the catalog and a `HelpFormatter` strategy through its constructor. `CommandProcessor` receives a `Map<String, HelpGenerator>` keyed by the help command (`HELP`, `HELP EXTENDED`), so new help variants need no change to command processing.

9. **BCrypt.** `PasswordHasher` interface with `BCryptPasswordHasher(logRounds)`. The initializer injects cost 10 (jBCrypt default) in production; tests inject cost 4. `User` stores the hash; `UserController` validates the plain password first, then hashes it. A malformed stored hash counts as a mismatch.

10. **Persistence.** `attractions.txt`: `id;name;capacity;type` (3 fields read as GENERAL). `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;true|false`, linked to tickets by id on load (loaded after tickets). Other formats unchanged; `users.txt` holds hashes.

11. **JAR.** `maven-shade-plugin` 3.6.0 creates `target/amusement-park.jar` with jBCrypt included and `Main-Class` set (the shaded JAR replaces the main artifact).

## Risks / Trade-offs

- [Large mechanical move of classes] → Done in one step with the full test suite re-run. Tests stay in `es.upm.amusementpark` and use public APIs.
- [Singletons keep state across tests] → Each test re-runs `ApplicationInitializer.initialize(tempDir, fastHasher)`, which reconfigures all singletons.
- [BCrypt cost slows tests] → Tests inject a low-cost hasher.
- [Shaded JAR warnings about overlapping manifest resources] → Harmless; jBCrypt has no service files.
