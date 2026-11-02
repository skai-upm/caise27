# Design

## Context

End of Delivery 2: packages `command`, `user`, `group`, `ticket`, `attraction`, `park`, `persistence` under `es.upm.etsisi.amusementpark`. `park.Park` (singleton) holds all state and every operation; users go through `user.UserService`; `persistence.ParkStorage` saves all files all-or-nothing; `Park` imports the concrete `ParkStorage` (business → persistence dependency). Passwords are plaintext. See proposal.md for motivation and `specs/` for behaviour.

## Goals / Non-Goals

**Goals:**
- Three layers with dependencies pointing presentation → business ← persistence (persistence implements a business-owned repository interface).
- Each business area has one singleton controller, wired by a hand-written composition root.
- One generic attraction mechanism where the type parameter is what decides acceptance.
- Every multi-object operation (pack purchase, group SPEED entry, cascade delete) validates fully before mutating, and is undone if persistence fails.

**Non-Goals:**
- Migrating plaintext `users.txt` from earlier deliveries; persisting invoices; persisting occupants.

## Decisions

### D1. Packages (F3-C01)
- `es.upm.etsisi.amusementpark` — `App` (entry point) and `ApplicationContext` (composition root: builds repositories, configures controllers, builds presentation). Neither is presentation logic.
- `presentation` — `CommandProcessor`, `CommandRunner`, `CommandResult`, `CommandRegistry`/`CommandSpec`, command modules (`UserCommands`, `ParkCommands`, `AttractionCommands`, `InvoiceCommands`), `InvoiceFormatter`, `presentation.help` (`HelpGenerator`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`).
- `business` — exceptions; `business.park` (`Park`, `ParkRepository`, `ParkData`, `Prices`), `business.user`, `business.group`, `business.ticket`, `business.pass`, `business.attraction`, `business.invoice`.
- `persistence` — `FileParkRepository` (was `ParkStorage`), per-entity file repositories, `TextFiles`, `TemporaryDataDirectory`.
*Alternative:* move `App` to `presentation` — rejected because it also wires persistence, which presentation must not touch.

### D2. Exceptions
Checked `business.BusinessException` (abstract) with `NotFoundException`, `InvalidDataException`, `OperationNotAllowedException` (state rules such as locks, dates, capacity) and `StorageException` (wraps `IOException` on save). Presentation adds `CommandSyntaxException` for arity/format errors. `CommandProcessor` catches both and returns `ERROR: <message>`; processing continues. Replaces the former `UserException`/`ParkException`.

### D3. Singletons and injection (F3-C03)
`Park` (state, date, price, `commit(undo)`), `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController` each have a private constructor, `getInstance()` (holder idiom) and `configure(...)` receiving their collaborators. `Park.configure(ParkRepository, LocalDate)` loads state. Collaborations: `UserController(Park, PasswordHasher, TicketController, GroupController)`, `GroupController(Park)`, `TicketController(Park, PassController)`, `PassController(Park)`, `AttractionController(Park, PassBook = PassController)`, `InvoiceController(Park)`. `App` creates `FileParkRepository(dataDir)` and passes it to `ApplicationContext.start(ParkRepository, LocalDate, PasswordHasher)`, which wires everything; `--fly` passes the temp directory. Repositories are ordinary objects. `ParkRepository` (business) has `load()`/`save(ParkData)`; tests inject an in-memory implementation.

### D4. Help (F3-C04)
Commands register a `CommandSpec(name, syntaxes, description, arities, handler)` in a `CommandRegistry`. `HelpGenerator(CommandRegistry, HelpFormatter)` formats `registry.specs()`; `ApplicationContext` injects `SyntaxHelpFormatter` for `HELP` and `ExtendedHelpFormatter` for `HELP EXTENDED`. A new format = a new `HelpFormatter`.

### D5. BCrypt (F3-C05)
`pom.xml` adds `org.mindrot:jbcrypt:0.4`; `maven-shade-plugin` builds `target/nombre-aplicacion.jar` with it so `java -jar` still works standalone. `business.user.PasswordHasher` interface with `BCryptPasswordHasher(int logRounds)` (production 10; tests 4 for speed). `User` holds `passwordHash`; `UserController` validates the plaintext with `UserValidator`, then hashes. `matches` catches `IllegalArgumentException` from non-BCrypt values and returns false (old plaintext files fail authentication).

### D6. Passes (F3-C06)
`business.pass.Pass` (abstract: `id`, `IndividualTicket ticket`, `BigDecimal price`, `boolean consumed`, `consume()`), `SpeedPass` (price = 20% of ticket price, HALF_UP, 2 decimals) and `SpecialPass` (price = ticket price). The pass knows its ticket; tickets do not know passes; `PassController` finds passes by traversing `Park`'s pass list, which also implements the lookups attractions need (`PassBook`). Shared pass id space for both types. A refunded Special Pass no longer exists, so a new one may be bought for that ticket; a consumed one blocks a second purchase.

### D7. Generic attractions (F3-C07/08)
- Access interfaces in `business.ticket`: `AccessTicket` (`getId`, `isValidOn`, `getIndividualTickets`), `IndividualAccess extends AccessTicket`, `GroupAccess extends AccessTicket`. `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
- `Attraction<T extends AccessTicket>` (abstract) keeps `Class<T> accepted`; `accepts(ticket)` = `accepted.isInstance(ticket)`. `prepareEntry(Ticket, boolean speed, PassBook)` casts to `T`, checks validity, type-specific pass rules (`selectPasses(T, speed, passes)` hook), duplicates and capacity, and returns an `Entry` (entrants + passes to consume) without mutating; `AttractionController` then admits and consumes, persists, and undoes both if saving fails.
- `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (requires the unconsumed Special Pass, rejects SPEED). `AttractionType` enum creates them.
- A group-ticket constituent id (e.g. `TG01:12345678Z`) is an `IndividualTicket`, so it is accepted wherever individual tickets are (as in Delivery 2).

### D8. Invoices (F3-C02)
`business.invoice`: immutable `Invoice(title, List<InvoiceSection>)` with `total()`; `InvoiceSection(title, List<InvoiceLine>)`; `InvoiceLine(description, amount, counted)` — detail lines (group composition) have `counted = false` and are shown without adding to the total. `InvoiceBuilder` interface (`addHeader`, `build`) + `AbstractInvoiceBuilder` (section assembly, omits empty sections) + `UserInvoiceBuilder` (`forUser`, `withTickets`, `withSpeedPasses`, `withSpecialPasses`) and `GroupInvoiceBuilder` (`forGroup`, `withGroupTickets`). `InvoiceController` acts as director. `presentation.InvoiceFormatter` renders text.

### D9. Persistence (F3-C09)
- `attractions.txt`: `id;name;capacity;TYPE`; 3-field lines → GENERAL.
- `passes.txt`: `SPEED|SPECIAL;passId;ticketId;price;consumed`; ticket resolved by id over standalone tickets and constituents; unknown tickets are skipped with a warning.
- `users.txt` unchanged in shape; 5th column is the hash.
- Cascade deletes remove passes and tickets in memory, then one `commit` saves all files with restore-on-failure; undo restores the list positions.

### D10. UML
`uml/model.puml` rewritten for the final classes: layers as packages, `<<singleton>>` controllers, injected dependencies (`-->` from controllers to collaborators and from `Park` to `ParkRepository`, `FileParkRepository ..|> ParkRepository`), access interfaces, `Attraction<T extends AccessTicket>` and its four bound subclasses, passes (`SpeedPass "*" --> "1" IndividualTicket`, `SpecialPass "0..1" --> "1" IndividualTicket`), and the invoice builder hierarchy. Checked with PlantUML `-syntax`.

## Risks / Trade-offs

- [Prescribed Git history: 10 separate commits `[F3-C01] …` to `[F3-C10] …`] → The BOTL batch harness owns Git history and records one commit per proposal, so these milestone commits are **not** produced; implementation follows checkpoint order per task group. Flagged, not silently resolved.
- [Ambiguity: "at most one Special Pass throughout its entire existence" vs refunds that remove the pass from the system] → Interpreted as: at most one Special Pass exists per ticket at any time; a consumed one blocks repurchase; a refunded one does not. Strict lifetime tracking would need a persisted marker the source does not describe.
- [Ambiguity: which SpeedPass is consumed] → The first unconsumed one in purchase order; its id is printed.
- [Existing plaintext users files fail authentication] → Accepted by the source ("automatic migration is not required").
- [BCrypt cost slows tests] → Rounds are injected; tests use 4.
- [Shaded JAR changes the build] → Needed so the executable JAR still runs with only `java -jar`.
- [Large package move] → Done with `git mv` to keep history; full regression suite re-run.
