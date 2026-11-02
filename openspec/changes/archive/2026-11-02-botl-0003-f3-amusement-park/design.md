# Design

## Context

Delivery 2 lives in `es.upm.etsisi.amusementpark` with packages `command` (parser, runner), `user`, `group`, `ticket`, `attraction`, `park` (`Park` singleton that builds its own storages from `DataPaths`), `persistence` (`*Storage` classes, `TemporaryDirectory`) and a single `OperationException`; 156 JUnit tests; `App` handles `[file]`, `--fly [file]`. See proposal.md for scope and specs/ for behavior.

## Goals / Non-Goals

**Goals:**
- Package layering that a reviewer can verify by imports: presentation → business; persistence → business; business → neither (it owns the repository interfaces).
- Keep every Delivery 1/2 behavior and test (moved, not rewritten), except the documented password-storage change.
- One code path for entry checks; pass consumption atomic with entry.

**Non-Goals:**
- Migrating plaintext `users.txt` files (explicitly not required).
- Persisting invoices, occupancy, system date or price.

## Decisions

### Package layout (F3-C01)
```
es.upm.etsisi.amusementpark.App            composition root (wiring, argument handling)
presentation        CommandProcessor, CommandRunner, CommandResult, CommandSpec, CommandCatalog,
                    InvalidCommandException, help/{HelpGenerator, HelpStrategy, SyntaxHelp, ExtendedHelp}
business            ParkException hierarchy
business.user       User, UserField, UserValidator, UserManager, Passwords
business.group      Group
business.ticket     Ticket, IndividualTicket, GroupTicket, AccessTicket, IndividualAccess, GroupAccess
business.pass       Pass, SpeedPass, SpecialPass, PassBook
business.attraction Attraction<T>, GeneralAttraction, IndividualAttraction, GroupAttraction, SpecialAttraction, AttractionType
business.invoice    Invoice, InvoiceSection, InvoiceLine, InvoiceBuilder, UserInvoiceBuilder, GroupInvoiceBuilder
business.controller ParkController, InvoiceController
business.repository UserRepository, GroupRepository, TicketRepository, AttractionRepository, PassRepository, Repositories
persistence         FileUserRepository, FileGroupRepository, FileTicketRepository, FileAttractionRepository,
                    FilePassRepository, FileRepositories, TemporaryDirectory
```
Repository interfaces live in business (dependency inversion), so business does not depend on persistence. `App` stays in the root package as the composition root.

### Exceptions (F3-C01)
Abstract checked `business.ParkException` with `InvalidDataException` (bad values), `NotFoundException` (unknown user/group/ticket/attraction/pass), `RuleViolationException` (locks, dates, capacity, duplicates, access type), `StorageException` (wraps repository `IOException`). Presentation adds `InvalidCommandException` (unknown command, wrong arity, bad option). `CommandProcessor` catches `ParkException` and prints `ERROR: <message>`; runtime exceptions are not used for expected rejections.

### Singletons and injection (F3-C03)
`ParkController` and `InvoiceController` keep a private constructor and `getInstance()`. `ParkController.configure(Repositories)` receives the repository set and loads state (replacing Delivery 2 `load(DataPaths)`); `InvoiceController.configure(ParkController)` receives its collaborator. `App` builds `FileRepositories.in(dataDir)` (working directory or the `--fly` temp directory), injects it, and builds `CommandProcessor(park, invoices, help, extendedHelp)`. Repositories are ordinary objects (not singletons). Tests inject `FileRepositories.in(tempDir)` or a failing stub.

### Help (F3-C04)
`CommandCatalog` holds `CommandSpec(syntax, description)` for every command; `HelpGenerator(HelpStrategy)` renders a catalog. `SyntaxHelp` lists syntaxes (`HELP`); `ExtendedHelp` adds descriptions (`HELP EXTENDED`). `CommandProcessor` gets both generators injected and contains no formatting logic. Command matching uses the catalog: keyword = text before the first `;`, arity = number of `;`-separated elements, so variants (`ATTRACTION CREATE` 4/5 elements, `ATTRACTION ENTER` 3/4, `INVOICE CREATE;USER|GROUP`) are distinct specs sharing one handler.

### BCrypt (F3-C05)
Dependency `org.mindrot:jbcrypt:0.4`. `Passwords.hash(plain)` (`BCrypt.hashpw` with `gensalt()`) and `Passwords.matches(plain, hash)` (returns false instead of throwing for values that are not BCrypt hashes, e.g. legacy plaintext or reconstructed former users). `UserManager.create` validates the candidate's plaintext password and registers a new `User` holding the hash; `update(PASSWORD)` validates and stores a fresh hash. `User.hasPassword` delegates to `Passwords.matches`. The `maven-shade-plugin` (3.6.0) bundles jBCrypt into `target/nombre-aplicacion.jar` (main class kept in the manifest), so `java -jar` keeps working without classpath configuration.

### Passes (F3-C06)
Abstract `Pass` (identifier, `IndividualTicket ticket`, stored `price`, `consumed`); `SpeedPass` and `SpecialPass` set their price at construction from `ticket.getPrice()` (×0.20 with scale 2 HALF_UP, ×1). Direction pass → ticket only; a ticket's passes are found by traversing the pass list (consistent with Delivery 2's "obtain by traversal" rule). `ParkController` keeps `List<Pass>`; pass ids are unique among passes only. SpeedPass purchase checks `<id>:1..<id>:n` before creating any. A group ticket identifier is not an individual ticket and is rejected for pass purchase.

### Attraction types with bounded generics (F3-C07/08)
- Access interfaces: `AccessTicket` (identifier, `isValidOn`, `getUsers()`, `getEntryTickets()` — the individual tickets that enter); `IndividualAccess extends AccessTicket`, `GroupAccess extends AccessTicket`. `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
- `abstract class Attraction<T extends AccessTicket>` holds identity, capacity, occupants and `Class<T> accepted`; `enter(Ticket, boolean speed, LocalDate today, PassBook passes)` checks `accepted.isInstance(ticket)`, casts to `T` and runs the template: validity → not already inside → capacity → `selectPasses(T, speed, passes)` → admit + consume. `selectPasses` defaults to "one unused SpeedPass per entry ticket when `speed`, none otherwise".
- `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (overrides `selectPasses`: rejects `speed`, requires the ticket's unconsumed Special Pass).
- `AttractionType` enum creates the matching subclass and is persisted.
- `PassBook` (implemented by `ParkController`) answers "unused SpeedPass for ticket" and "Special Pass of ticket", so attractions do not depend on the controller.
- The registered-user check stays in `ParkController` before delegating. Consumption is persisted: after a successful entry the pass file is saved; on save failure occupants and passes are rolled back.

### Invoices (F3-C02)
`InvoiceBuilder` (abstract) offers the shared steps `title`, `recipient(lines…)`, `section(name, lines)` (skipped when empty) and `build()` (total = sum of counted line amounts). `UserInvoiceBuilder` adds `customer(User)`, `individualTickets(...)`, `speedPasses(...)`, `specialPasses(...)`; `GroupInvoiceBuilder` adds `group(Group)` and `groupTickets(...)` whose lines carry composition details (detail rows are not counted). `InvoiceController` acts as director: it gathers data from `ParkController` (all dates, historical included) and drives the builder. `Invoice.render()` produces the text shown.

### Persistence formats (F3-C09)
- `attractions.txt`: `id;name;capacity;TYPE` (3-field lines → GENERAL).
- `passes.txt`: `S|P;passId;ticketId;price;consumed` (`S` SpeedPass, `P` Special Pass); ticket ids resolve through the shared ticket id space (including composition ids). Passes whose ticket is missing are skipped with a warning.
- Load order: users → groups → tickets → attractions → passes. Cascading deletes save tickets and passes; on failure both lists are restored and re-saved (best effort).

### UML (F3-C09)
`uml/model.puml` is rewritten from the final code (controllers marked `<<singleton>>`, repository interfaces and injection dependencies, access interfaces, generic attractions with bound parameters, pass cardinalities `IndividualTicket "1" <-- "0..*" SpeedPass` and `"0..1" SpecialPass`, builder hierarchy), and verified with the cached PlantUML jar (`-checkonly` and Smetana render).

### Git milestone history
As in Deliveries 1 and 2, the 10 prescribed milestone commits (`[F3-C01] …` to `[F3-C10] …`) are not produced; the batch harness owns Git history and makes one commit per proposal. Flagged deviation from the source's process requirement.

## Risks / Trade-offs

- [Source: a ticket may buy at most one Special Pass "throughout its entire existence", yet a refunded pass "ceases to be part of the persisted state"] → Interpreted as: at most one Special Pass associated at a time; a consumed one keeps blocking; after a refund no record remains, so a new purchase is allowed. Enforcing more would need invented persistent state.
- [Plaintext `users.txt` from Delivery 1/2 cannot authenticate after upgrading] → Accepted: migration is explicitly out of scope; `Passwords.matches` returns false instead of crashing.
- [BCrypt cost makes tests slower] → Default cost (10) kept for production fidelity; acceptable test time.
- [Shaded JAR replaces the plain JAR artifact] → Same `target/nombre-aplicacion.jar` path; verified with `java -jar`.
- [SPEED with a composition ticket id at a GENERAL attraction] → Treated like any individual ticket (needs its own SpeedPass), consistent with Delivery 2's decision that composition ticket ids are usable for entry.
- [Large package move] → Done mechanically (move + package/import rewrite) before behavior changes, with the full suite run in between.
