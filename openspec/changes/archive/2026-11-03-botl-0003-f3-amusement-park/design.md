# Design

## Context

Delivery 2 (see `openspec/specs/`) is a flat package `es.upm.etsisi.amusementpark`: `App`, `CommandProcessor`, the `ParkController` singleton (all state and operations, save-all commit with undo), domain classes (`User`, `Group`, `Ticket` ← `IndividualTicket`/`GroupTicket`, `Attraction`), text repositories aggregated by `ParkRepositories`, `FlyWorkspace`, `TextFile`, `Values`, `ParkException`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Package dependencies flow `presentation → business ← persistence`, with `App` as composition root.
- Every state-changing command, including entry with pass consumption and cascading deletions, stays all-or-nothing in memory and on disk.
- Generic attraction types whose type parameter restricts which tickets the entry operation accepts.

**Non-Goals:**
- Migrating plaintext `users.txt` files from earlier deliveries.
- Persisting invoices or attraction occupants.

## Decisions

1. **Packages.**
   - `es.upm.etsisi.amusementpark` — `App` (composition root: builds repositories, hasher, controllers, help generators and the command processor; `--fly` handling).
   - `…presentation` — `CommandProcessor`, `CommandCatalog` (syntax + purpose of every command), `HelpGenerator`, `HelpFormat` strategy with `SyntaxHelpFormat` and `ExtendedHelpFormat`.
   - `…business` — exceptions, `User`, `UserField`, `UserValidator`, `Values`, `PasswordHasher`; `…business.ticket` — `Ticket`, `IndividualTicket`, `GroupTicket`, access interfaces, `Pass`, `SpeedPass`, `SpecialPass`; `…business.group` — `Group`; `…business.attraction` — attraction types; `…business.repository` — repository interfaces and the `ParkRepositories` bundle; `…business.controller` — `ParkController`, `InvoiceController`; `…business.invoice` — `Invoice` and builders.
   - `…persistence` — `TextUserRepository`, `TextGroupRepository`, `TextTicketRepository`, `TextAttractionRepository`, `TextPassRepository`, `TextFile`, `TextRepositories` factory, `FlyWorkspace`, `BCryptPasswordHasher`.
2. **Exceptions.** Checked `ParkException` base with `InvalidDataException` (malformed or invalid input), `NotFoundException`, `RuleViolationException` (business rule refused) and `StorageException` (I/O). Repositories throw `StorageException`; the controller throws the business ones; only `CommandProcessor` and `App` turn them into `ERROR: …` lines, so processing always continues.
3. **Singletons and injection.** `ParkController` and `InvoiceController` keep a private constructor and a static instance. `ParkController.initialize(ParkRepositories, PasswordHasher, LocalDate)` receives every repository (interfaces) and the hasher; `InvoiceController.configure(ParkController)` receives its collaborator. Repositories are ordinary objects created per run by `TextRepositories.inDirectory(path, warnings)`, so normal and `--fly` runs differ only in the directory. No DI framework.
4. **Help.** `HelpGenerator(CommandCatalog, HelpFormat)`; `App` injects one generator per command (`HELP` → `SyntaxHelpFormat`, `HELP EXTENDED` → `ExtendedHelpFormat`) into `CommandProcessor`, which only looks the generator up by keyword. New formats only need a new `HelpFormat`.
5. **BCrypt.** `PasswordHasher` interface (`hash`, `matches`) in business; `BCryptPasswordHasher` in persistence wraps jBCrypt with a configurable cost (10 in `App`, 4 in tests for speed). `User` stores only the hash. Delivery-1 validation still runs on the plaintext before hashing. `matches` returns false for values that are not BCrypt hashes (e.g. legacy plaintext), so legacy users cannot authenticate — accepted because migration is explicitly not required. `maven-shade-plugin` bundles jBCrypt into `target/nombre-aplicacion.jar`.
6. **Access interfaces and generics.** `AccessTicket` (general access: `getEntryTickets()`, `getHolders()`), `IndividualAccess extends AccessTicket` (implemented by `IndividualTicket`), `GroupAccess extends AccessTicket` (implemented by `GroupTicket`). `Attraction` is a non-generic interface used by controllers and repositories; `AbstractAttraction<T extends AccessTicket>` holds identity, capacity, occupants and a `Class<T>` token, and its `admit(T ticket, …)` only accepts `T`; `enter(Ticket …)` checks/casts through the token, so a wrong ticket kind is rejected before any state changes. `GeneralAttraction<AccessTicket>`, `IndividualAttraction<IndividualAccess>`, `GroupAttraction<GroupAccess>`, `SpecialAttraction<IndividualAccess>` (adds the Special Pass rule, refuses SPEED). `AttractionType` enum creates instances. Using a non-generic interface for collections avoids wildcard types in public signatures.
7. **Passes.** Abstract `Pass` (identifier, `IndividualTicket`, stored price, consumed flag) with `SpeedPass` and `SpecialPass`; passes reference their ticket, tickets do not reference passes (unidirectional, like tickets→users). One pass identifier space. SpeedPass price = ticket price × 0.20, Special Pass price = ticket price, both rounded to cents and stored at purchase. A refunded Special Pass leaves the system, so a new one may be bought for that ticket; a consumed one stays and blocks another purchase.
8. **Entry.** `ParkController.enter(attraction, ticket, option)` checks date validity and registered holders, then delegates to the attraction, which checks ticket kind, SPEED acceptance, required passes (via a `PassLookup` supplied by the controller), presence and capacity before mutating anything; it then admits holders and marks passes consumed. The controller commits (passes' consumed state is persisted) with an undo that removes the admitted holders and un-consumes the passes.
9. **Cascades.** Valid `TICKET DELETE` / `GROUPTICKET DELETE` remove the associated passes together with the ticket(s) in one commit; the undo restores passes and tickets at their former positions.
10. **Invoices.** `Invoice` (title, header lines, sections of items with amount, total). Abstract `InvoiceBuilder<B>` holds the common steps (`header`, `section`, `build`) and computes the total from counted items; `UserInvoiceBuilder` adds `forUser`, `individualTickets`, `speedPasses`, `specialPasses`; `GroupInvoiceBuilder` adds `forGroup`, `groupTickets` (each group ticket one counted item, its composition as uncounted detail lines). Empty sections are skipped. `InvoiceController` acts as director using `ParkController` queries. User invoice: standalone tickets of the user + passes on any individual ticket held by the user (standalone or composed). Group invoice: the group's group tickets only.
11. **Persistence.** `attractions.txt` gains a fourth column `TYPE` (3-column lines load as GENERAL); new `passes.txt` with `SPEED|SPECIAL;identifier;ticketIdentifier;price;consumed`; `users.txt` keeps its seven columns with the hash in the password column. Passes reference tickets by identifier across the shared ticket space, loaded after tickets.

## Risks / Trade-offs

- [Each BCrypt hash costs ~0.1 s at cost 10] → Acceptable for interactive use; tests inject cost 4.
- [Large refactor (package move) may break Delivery 1/2 behaviour] → All Delivery 1/2 tests are kept and moved, and must pass unchanged in intent.
- [Composed-ticket passes appear on the member's invoice but not the group's] → Mandated by the source; covered by tests.

## Migration Plan

Old `attractions.txt` lines load as GENERAL; old `users.txt` lines load but plaintext passwords no longer authenticate (no migration, as allowed). Rollback is reverting the commit.

## Open Questions (flagged, not resolved by invention)

- **Mandatory milestone commits** `[F3-C01] …` to `[F3-C10] …` cannot be reproduced individually: the batch orchestrator records one commit per proposal (same limitation as Deliveries 1 and 2).
- **Special Pass after refund**: the source limits a ticket to one Special Pass "throughout its entire existence" but also says a refunded pass ceases to exist; this design allows a new purchase after a refund (decision 7).
