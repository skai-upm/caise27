# Design

## Context

Current code (after F2) lives in `es.upm.amusementpark`: `App`, `CommandProcessor`, `ParkController` (system date, base price, cross-entity rules) over `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, each saving through a `*Storage` file class located by `StoragePaths`; domain `User`, `Group`, `Ticket`/`IndividualTicket`/`GroupTicket`, `Attraction`; `OperationException` for rejections. 98 tests. See proposal.md for motivation; specs for behavior.

## Goals / Non-Goals

**Goals:** layered packages with one-way dependencies (presentation → business ← persistence); controllers as per-execution Singletons with injected collaborators; every F3 rule testable without the terminal; all multi-object changes all-or-nothing in memory and on disk.

**Non-Goals:** DI frameworks, databases, migrating legacy plaintext passwords, new commercial rules beyond the source.

## Decisions

### D1. Packages (F3-C01)
- `presentation`: `App`, `AppBootstrap` (wiring), `CommandProcessor`, help classes (`CommandInfo`, `CommandRegistry`, `HelpFormatter`, `ConciseHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`).
- `business.model`: `User`, `UserField`, `UserValidator`, `Group`, ticket hierarchy, passes, attractions, invoice classes, `OperationException`.
- `business.controller`: controllers and `PasswordHasher` abstraction.
- `business.repository`: repository interfaces (`UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`).
- `persistence`: file implementations (`UserFileRepository`, …), `StoragePaths`, `TextFiles` (atomic write helper), `BCryptPasswordHasher`.
Business depends only on its own interfaces; persistence implements them (dependency inversion), so business never imports persistence and repositories stay replaceable. `Manager` classes are renamed `Controller` (they are the unique management controllers) and `*Storage` become `*FileRepository`.
*Alternative:* interfaces in `persistence` — rejected; it creates a business↔persistence package cycle.

### D2. Singleton controllers + injection (F3-C03, open question 2)
Controllers: `UserController`, `GroupController`, `TicketController` (tickets and passes), `AttractionController`, `InvoiceController`, `ParkController` (main controller: date, price, cross-entity rules). Each has a private constructor, `static initialize(deps…)` (fails if already initialized), `static getInstance()` and `static reset()`. `AppBootstrap.start(paths, hasher, startDate)` creates the repositories for the chosen `StoragePaths`, then initializes controllers in dependency order, passing repositories and already-initialized controllers; `AppBootstrap.shutdown()` resets them when the execution ends (so tests and repeated `App.run` calls in one JVM each get one fresh instance per execution). Repositories are plain objects, never Singletons; `--fly` only changes the `StoragePaths` given to the repository constructors.

### D3. Invoices with Builder (F3-C02, open question 1)
`InvoiceBuilder` (interface: `recipient(kind, id, name)`, `section(title)`, `item(description, amount)`, `build()`) and `SimpleInvoiceBuilder` producing an immutable `Invoice` (recipient, ordered sections of `InvoiceLine`, total computed in `build()`). `InvoiceController` acts as director and receives a `Supplier<InvoiceBuilder>` at startup; it opens a section only when it has items. At F3-C02 the user invoice has the "Individual tickets" section only; F3-C06 adds the "Personal passes" section (passes from all the user's individual tickets, including group-owned ones). Group invoice: "Group tickets" section. Output text: header, sections with `id  date  price` lines, `TOTAL: x.xx`.

### D4. Help (F3-C04)
`CommandRegistry` holds `CommandInfo(syntax, description)` for every command. `HelpGenerator` receives the registry and a `Map<String, HelpFormatter>` keyed by mode (`""` concise, `"EXTENDED"`) at startup; `CommandProcessor` receives the `HelpGenerator` and only calls `generate(mode)`. A new format is a new `HelpFormatter` registered in `AppBootstrap`.

### D5. Passwords (F3-C05, open question 5)
Dependency `org.mindrot:jbcrypt:0.4`. `PasswordHasher` (business interface: `hash`, `matches`) with `BCryptPasswordHasher(logRounds)` in persistence-side infrastructure (`persistence` package, injected by bootstrap; tests use 4 rounds for speed, the app 10). `UserController` validates the E1 format, then stores `hasher.hash(pw)`. Verification: if the stored value looks like a BCrypt hash (`$2a$`/`$2b$`/`$2y$`, 60 chars) use `BCrypt.checkpw`; otherwise it is a legacy plaintext value from E1/E2 files and is compared literally — it keeps working but is not migrated automatically (only replaced when the user changes the password). `maven-shade-plugin` bundles jbcrypt into `target/parque.jar`.

### D6. Passes (F3-C06, open question 3)
`abstract Pass { id, price, consumed }`, `SpeedPass`, `SpecialPass`. `IndividualTicket` owns `List<SpeedPass>` and an optional `SpecialPass` plus `specialPassAcquired` (lifetime flag; refunding does not reset it). Source gives no command arguments, so: `SPEEDPASS CREATE;IdTicket;Cantidad`, `SPECIALPASS CREATE;IdTicket`, `SPEEDPASS DELETE;IdPase`, `SPECIALPASS DELETE;IdPase`. IDs: `<ticketId>-SP<n>` (n = highest existing + 1, never reused within the ticket's live passes) and `<ticketId>-SPECIAL`. Applicable to any individual ticket, including group-owned ones; a group-ticket id is rejected. Purchase requires ticket date ≥ system date (the ticket can still be used); refund requires ticket date > system date (E2 refund rule) and unused pass. SpeedPass price = ticket price × 0.20 (HALF_UP, 2 decimals); Special Pass price = ticket price.

### D7. Typed attractions with bounded generics (F3-C07, open question 6)
Interfaces in `business.model`: `AttractionAccess` (`getId`, `isValidOn`, `getUsers`, `getAccessTickets()` → the individual tickets that admit people), `IndividualAccess extends AttractionAccess` (`getUser`, `getSpecialPass`), `GroupAccess extends AttractionAccess` (`getGroup`, `getTickets`). `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
`abstract class Attraction<T extends AttractionAccess>` holds `Class<T> accessType`; `enter(Ticket, speed, date)` rejects tickets that are not `accessType.isInstance`, casts to `T` and delegates to typed hooks `checkPasses(T, speed)` / `consumePasses(T, speed)`. Subclasses: `GeneralAttraction extends Attraction<AttractionAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (uses `getSpecialPass`, rejects SPEED). `AttractionType` enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) creates the right subclass. The bound is functional: the hooks receive the typed access object and use its capabilities.

### D8. Pass-based entry (F3-C08)
Order inside `enter`: type check → date → duplicates → capacity → pass preflight (every access ticket has an unused SpeedPass when SPEED; Special Pass unused for SPECIAL) → mutate occupancy → consume passes. All checks happen before any mutation, so failures change nothing. The reply lists consumed pass ids. After consumption `TicketController.save()` persists pass states; if the save fails, occupancy and pass states are rolled back.

### D9. Persistence & consistency (F3-C09, open question 4)
`tickets.txt` adds: `I`/`C` lines get a trailing `specialPassAcquired` flag (`0`/`1`; F2 lines without it read as `0`); `S;ticketId;passId;price;consumed` (SpeedPass) and `X;ticketId;passId;price;consumed` (SpecialPass) follow their ticket. Passes live in the same file as their tickets, so every cascade deletion is a single write. `TextFiles.writeAtomically` writes to a sibling temp file then `Files.move(…, ATOMIC_MOVE, REPLACE_EXISTING)` (falls back to `REPLACE_EXISTING` if atomic move is unsupported). Deletion preflight (date rule, ownership) runs before mutation; the in-memory removal order is passes → constituents → group ticket; a failed write restores the previous in-memory list. `attractions.txt`: `id;name;capacity;TYPE`; 3-column lines load as GENERAL. `--fly` needs nothing new: all new data lives in files under the configured `StoragePaths`.

### D10. Commits
After each milestone's tasks pass `mvn test`, commit with the exact mandated subject (`[F3-C01] Reorganize application into layered packages.` … `[F3-C10] Complete delivery 3 integration`), staging only code/test/build/UML files for that milestone. Planning artifacts and the archived change are committed with the batch commit after archive. Nothing is pushed until the end.

## Risks / Trade-offs

- [Singletons with `reset()` are weaker than strict Singletons] → `reset` is only called by `AppBootstrap.shutdown` at the end of an execution (and by tests), preserving "one instance per execution".
- [Legacy plaintext passwords remain readable in old files] → Explicitly out of scope; replaced on next password change.
- [BCrypt slows tests] → Injected hasher with 4 log rounds in tests.
- [`SPECIAL` refunds do not allow repurchase] → Follows "at most one … throughout its lifetime".
- [Large rename/move in C01 makes history noisy] → Done with `git mv` in one dedicated milestone commit.
