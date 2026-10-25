# Design

## Context

After Delivery 2 all 19 classes live in `es.upm.parque`: `App` (args, `--fly`, input loop), `CommandProcessor` (switch on operation), `ParkController` (date, price, cross-manager orchestration, static `load(dataDir)` wiring), four `*Manager` classes, four `*Repository` classes, the model (`User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`) and `UserValidator`. 85 tests exist in the same package. `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` 3.6.0 are available in the local Maven repository. See proposal.md for motivation and the delta specs for behavior.

## Goals / Non-Goals

**Goals:**
- Physical layers with no behavior change for E1/E2 commands.
- Singletons for controllers; repositories created and injected in one composition root (`App`).
- Each new feature isolated in its own classes so it maps to one of the ten mandatory commit topics.

**Non-Goals:**
- An IoC framework; persistence formats other than text files; persisting invoices; a GUI.

## Decisions

1. **Packages.**
   - `es.upm.parque.App` — composition root only (args, `--fly` directory, wiring, start runner).
   - `presentation`: `CommandProcessor`, `CommandDefinition` (record: name, parameter names with optional ones in brackets, description, action), `CommandAction` (functional interface), `InputRunner` (line loop for stdin/file), `presentation.help`: `HelpFormatter` (interface), `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`.
   - `business.model`: `User`, `UserField`, `Group`, `Access`, `IndividualAccess`, `GroupAccess`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Pass`, `SpeedPass`, `SpecialPass`, `Attraction<T extends Access>`, `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, `EntryResult`.
   - `business.controller`: `ParkController` (main: date, price, orchestration), `UserController`, `GroupController`, `TicketController` (tickets and passes), `AttractionController`, `InvoiceController`.
   - `business.invoice`: `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` (interface), `StandardInvoiceBuilder`.
   - `business.validation`: `UserValidator`. `business.security`: `PasswordHasher` (BCrypt wrapper, configurable log rounds).
   - `persistence`: `UserRepository`, `GroupRepository`, `TicketRepository`, `PassRepository`, `AttractionRepository`, `StorageLocation` (builds every file path from a data directory).
   Tests move to the matching packages.

2. **Singleton with injected dependencies.** Each controller has a private constructor, a `private static final` instance, `getInstance()` and a `configure(...)` method receiving repositories/collaborators and loading data. `configure` can be called again (new run or test) and replaces state — this keeps the single instance while allowing normal vs `--fly` configuration. `App.initialize(Path dataDir, PasswordHasher hasher)` performs: repositories from `StorageLocation` → `UserController.configure(userRepo, hasher)` → `GroupController.configure(groupRepo, users)` → `TicketController.configure(ticketRepo, passRepo, users, groups)` → `AttractionController.configure(attractionRepo)` → `InvoiceController.configure(tickets, StandardInvoiceBuilder::new)` → `ParkController.configure(users, groups, tickets, attractions, invoices)`.
   *Alternative:* enum singletons — rejected, harder to reconfigure per run.

3. **Commands as registered definitions.** `CommandProcessor` builds a `LinkedHashMap<String, CommandDefinition>`; dispatch looks up the operation, checks min/max parameter counts derived from the parameter names, and invokes the action. `HELP` and `HELP EXTENDED` call `HelpGenerator.generate(mode, definitions)`. `HelpGenerator` receives a `Map<String, HelpFormatter>` from `App` (`BASIC` → syntax, `EXTENDED` → syntax + description), so new formats only need a new formatter registered at startup.

4. **Password hashing.** `PasswordHasher.hash/matches` using `BCrypt.hashpw(gensalt(rounds))`/`checkpw`. Production uses 10 rounds; tests inject 4 rounds for speed. `User.password` stores the hash. `UserController.configure` migrates any stored value that is not a `$2a$/$2b$/$2y$` 60-character hash and re-saves.

5. **Access interfaces and bounded generics.** `Access { getId(); getDate(); getUsers(); getIndividualTickets(); }`, `IndividualAccess extends Access { getUser(); getSpecialPass(); }`, `GroupAccess extends Access { getGroup(); }`. `Ticket implements Access`; `IndividualTicket implements IndividualAccess`; `GroupTicket implements GroupAccess`. `abstract class Attraction<T extends Access>` keeps `Class<T> accessType`; `enter(Access, date, speed)` rejects tickets not `accessType.isInstance`, casts to `T` and runs the common checks plus the hook `checkSpecific(T, speed)` / `consumeSpecific(T)`. Subclasses: `GeneralAttraction extends Attraction<Access>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (requires unconsumed Special Pass, rejects SPEED, consumes the pass after entry). `AttractionType` enum creates the right subclass.

6. **SpeedPass logic.** For SPEED, every ticket in `getIndividualTickets()` must have an unconsumed SpeedPass (checked before any change); after users are added, the first unused SpeedPass of each is consumed. `EntryResult` returns success, message and consumed pass ids. `TicketController.savePasses()` persists consumption.

7. **Passes.** `abstract Pass { id, price, consumed }`; `SpeedPass`, `SpecialPass`. `IndividualTicket` composes `List<SpeedPass>` and at most one `SpecialPass`. Ids: `<ticket>-SP<n>` (n = max existing + 1) and `<ticket>-SPECIAL`. Prices: SpeedPass `ticketPrice × 0.20`, Special = ticket price (2 decimals). Purchase requires ticket date ≥ current date; refund requires unconsumed and ticket date > current date. A refunded Special Pass is removed, so another may be bought later (the "only one" rule applies to existing passes, consumed ones are never removed).

8. **Pass persistence and cascades.** `passes.txt`: `id;SPEED|SPECIAL;ticketId;price;consumed`, loaded after tickets and attached by ticket id. Because passes are owned by tickets, deleting a ticket from memory drops its passes; deletions validate everything first, then remove group ticket → internal tickets → passes from memory, then rewrite `passes.txt`, `tickets.txt`, `grouptickets.txt`. `attractions.txt` adds a 4th column (type); 3-column lines load as GENERAL.

9. **Invoices.** `InvoiceBuilder` steps: `reset()`, `setRecipient(String)`, `setIssueDate(LocalDate)`, `startSection(String)`, `addItem(String, BigDecimal)`, `getResult()`. `InvoiceController` is the director: it starts a section only when there are items for it (user: "Individual tickets" for standalone tickets of the user; "Personal passes" for passes on all the user's individual tickets including internal ones; group: "Group tickets"). `Invoice.toString()` renders items with prices and `TOTAL`.

10. **Fat JAR.** `maven-shade-plugin` 3.6.0 at `package` with `ManifestResourceTransformer(mainClass=es.upm.parque.App)` replaces `target/parque.jar`; the jar-plugin manifest stays for consistency.

11. **UML.** `uml/model.puml` rewritten with packages, keeping the D2 conventions (no methods, `x-->` associations named after attributes, composition/aggregation, usage only for classes without incoming relationships, generic bindings shown on inheritance edges).

12. **Mandatory commits.** The BOTL batch protocol produces one commit per proposal; the ten `[F3-Cxx]` subjects are recorded in that commit's body and task groups map to them one-to-one.

## Risks / Trade-offs

- [Singletons hold state across tests] → `configure` fully resets state; tests always configure with a fresh temp directory.
- [BCrypt slows tests] → injected low cost factor in tests.
- [Large mechanical move may break package-private access] → make cross-package members public deliberately; run full test suite after the move before adding features.
- [Shade warns about overlapping manifest files] → harmless; jbcrypt has no conflicting resources.
