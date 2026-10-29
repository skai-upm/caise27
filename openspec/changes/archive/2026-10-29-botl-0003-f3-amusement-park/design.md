# Design

## Context

The Delivery 2 code lives entirely in `es.upm.amusementpark`. `ParkController` is a singleton that builds its own repositories in `initialize(dataDir)`, and `UserService` loads users. Errors are `IllegalArgumentException`. See proposal.md for the Delivery 3 scope.

## Goals / Non-Goals

**Goals:**
- Clear layers, and controllers that receive all dependencies from one composition root.
- Generic attraction types whose type parameter actually restricts the tickets they accept.
- Atomic multi-pass entry and atomic cascading deletes.

**Non-Goals:**
- Migrating plain-text `users.txt` from earlier deliveries.
- Persisting invoices or attraction occupants.

## Decisions

### Packages (root `es.upm.amusementpark`)

| Package | Contents |
|---|---|
| `App` (root) | `main` plus the composition root `AppInitializer`, which wires repositories, controllers and presentation |
| `presentation` | `CommandProcessor`, `CommandRunner`, `CommandCatalog`/`CommandInfo`, `HelpGenerator`, `HelpFormatter` (strategy), `SyntaxHelpFormatter`, `ExtendedHelpFormatter` |
| `business.model` | `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `GeneralAccess`, `IndividualAccess`, `GroupAccess`, `Pass`, `SpeedPass`, `SpecialPass`, `Attraction<T>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, `EntryResult` |
| `business.controller` | `ParkController`, `UserController`, `PassController`, `InvoiceController` |
| `business.invoice` | `Invoice`, `InvoiceSection`, `InvoiceItem`, `InvoiceBuilder` (abstract), `UserInvoiceBuilder`, `GroupInvoiceBuilder` |
| `business.validation` | `UserValidator`, `ValueParser` |
| `business.security` | `PasswordHasher` (interface), `BCryptPasswordHasher` |
| `business.exception` | `ParkException` (abstract, unchecked) → `ValidationException`, `NotFoundException`, `DuplicateException`, `OperationNotAllowedException`, `AuthenticationException`, `PersistenceException` |
| `persistence` | Repository interfaces `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, `PassRepository` |
| `persistence.text` | `Text*Repository` implementations and `TextFiles` |

Presentation depends only on `business`. Persistence depends on `business.model`/`exception`. The composition root is the only place that knows the concrete repositories.

### Exceptions
The exceptions are unchecked, so that controller signatures stay readable. `ParkException` is the single type caught by `CommandProcessor`, which prints `ERROR: <message>` and continues. `CommandProcessor` also catches any other `RuntimeException` as a last resort, so command processing never terminates. I/O errors in repositories are wrapped in `PersistenceException`.

### Singletons and injection
`ParkController`, `UserController`, `PassController` and `InvoiceController` each have a private constructor and `getInstance()`. `AppInitializer.initialize(Path dataDir, PasswordHasher hasher)` builds the five text repositories for `dataDir` and injects them through `configure(...)` methods:
- `UserController.configure(userRepo, hasher)`
- `ParkController.configure(userController, groupRepo, ticketRepo, attractionRepo, passRepo)`
- `PassController.configure(parkController)`
- `InvoiceController.configure(parkController, userController)`

It also builds the presentation `CommandProcessor` with the controllers and two injected `HelpGenerator`s. `--fly` simply calls `initialize` with the temporary directory. Repositories are plain objects, created per initialization. Tests use a low-cost `BCryptPasswordHasher(4)`, and production uses the BCrypt default (10).

### Ticket access capabilities and generic attractions
- `GeneralAccess` provides `getId()`, `isValidOn(LocalDate)`, `getHolders()` and `getEntryTickets()` (the individual tickets that enter). `Ticket implements GeneralAccess`.
- `IndividualAccess extends GeneralAccess`, implemented by `IndividualTicket`; it adds `getSpecialPass()`.
- `GroupAccess extends GeneralAccess`, implemented by `GroupTicket`; it adds `getComposition()`.
- `abstract class Attraction<T extends GeneralAccess>` holds `Class<T> accessType` and the template method `EntryResult enter(T access, LocalDate today, boolean speed)`:
  1. validate the date;
  2. call the hook `checkAccess(T, speed)`;
  3. select the passes to consume (one unconsumed SpeedPass per entry ticket, or the Special Pass, through the hook `selectPasses`);
  4. check capacity and duplicates;
  5. then, and only then, add the occupants and consume the selected passes.
- Subclasses:
  - `GeneralAttraction extends Attraction<GeneralAccess>`
  - `IndividualAttraction extends Attraction<IndividualAccess>`
  - `GroupAttraction extends Attraction<GroupAccess>`
  - `SpecialAttraction extends Attraction<IndividualAccess>`, which overrides pass selection to require the Special Pass and reject SPEED.
- `ParkController` bridges an unknown ticket to `Attraction<T>` with `accessType.isInstance`/`cast`. A ticket that does not implement `T` is rejected with an `OperationNotAllowedException`.

### Passes
- `abstract Pass` holds `id`, `price`, `ticket` and `consumed`. `SpeedPass` and `SpecialPass` extend it.
- `IndividualTicket` owns `List<SpeedPass>` (0..*) and `SpecialPass` (0..1) by composition, and each pass references its ticket. Deleting a ticket therefore removes its passes, which keeps cascades atomic: validation happens before the single `remove`.
- Pass ids are unique across all existing passes, checked by traversing the tickets.
- Prices: SpeedPass = ticket price × 0.20 (HALF_UP, 2 decimals); SpecialPass = ticket price.
- `PassController` implements purchase and refund using `ParkController` lookups and the current date, and saves through `ParkController`.

### Invoices
- `InvoiceBuilder` (abstract) holds the shared steps `reset()`, `header(title, lines…)`, `item(section, description, amount, details…)` and `build()`, which computes the total and drops empty sections. `UserInvoiceBuilder` adds `ticket(IndividualTicket)`, `speedPass(SpeedPass)` and `specialPass(SpecialPass)`, and `GroupInvoiceBuilder` adds `groupTicket(GroupTicket)` with its composition as details.
- `InvoiceController` acts as the director and gathers the data from `ParkController`. `Invoice.render()` produces the text.

### Help
`CommandCatalog` lists a `CommandInfo(syntax, description)` per command. `HelpGenerator(HelpFormatter formatter)` renders the catalog with the injected strategy; `SyntaxHelpFormatter` gives syntax lines and `ExtendedHelpFormatter` gives `syntax` plus an indented description.

### Persistence formats
- `attractions.txt`: `id;name;capacity;type` (3-field lines are read as GENERAL).
- `passes.txt`: `SPEED|SPECIAL;passId;ticketId;price;consumed(true|false)`, where `ticketId` resolves top-level or composition tickets.
- `users.txt`: the password column is the BCrypt hash. Other files are unchanged.

### Packaging
`maven-shade-plugin` (3.6.0, `package` phase) replaces the plain jar with a shaded `target/amusement-park.jar` that includes jbcrypt. The manifest main class becomes `es.upm.amusementpark.App`.

## Risks / Trade-offs

- [Ambiguity: Special Pass after a refund] → "At most one Special Pass" is enforced over the passes that currently exist. A refunded pass leaves the system, so a new one may be bought. A consumed pass stays and blocks repurchase.
- [Ambiguity: which SpeedPass is consumed] → The first unconsumed pass in purchase order is consumed, and its id is printed.
- [BCrypt cost in tests] → The work factor is injectable, and tests use 4.
- [Legacy plain-text `users.txt`] → Such users can no longer authenticate; the source states migration is not required.
- [Per-milestone commit messages] → Recorded in the single BOTL commit body, as before.
