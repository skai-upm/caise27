# Design

## Context

Current code (E1+E2) lives in package `es.upm.amusementpark`: `App`/`CommandProcessor` (presentation), `Park` (main controller holding system date and price, builds the services in its constructor), `UserService`, `GroupService`, `TicketService`, `AttractionService`, model classes (`User`, `Group`, `Ticket`→`IndividualTicket`/`GroupTicket`, `Attraction`), `UserValidator`, and file repositories plus `StorageLocation` (normal vs `--fly` paths). Requirements: this change's `specs/`; motivation: proposal.md. Milestones F3-C01…C10 are implemented and committed in order with the mandated messages.

## Goals / Non-Goals

**Goals:** clean layer boundaries; one composition root that wires everything at startup; generics that carry real constraints; all-or-nothing state changes for entries and cascade deletions; backward-compatible data files.

**Non-Goals:** DI frameworks, databases, new UIs, automatic migration of plaintext passwords, new access policies beyond the source.

## Decisions

1. **Packages (C01).**
   - `presentation`: `App`, `ApplicationBootstrap` (composition root, C03), `CommandProcessor`, `presentation.help` (C04).
   - `business.model`: `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, access interfaces, passes, attractions, `AttractionType`, `AccessDeniedException`, `Formats` (date parsing / price formatting shared by business and persistence).
   - `business.controller`: controllers. `business.validation`: `UserValidator`. `business.security`: `PasswordHasher`, `BCryptPasswordHasher`. `business.invoice`: `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `TextInvoiceBuilder`.
   - `persistence`: `StorageLocation`, repository interfaces and `File*Repository` implementations, `TextFiles` (safe writes).
   Tests mirror the packages.
2. **Invoices with Builder (C02, completed in C06).** `InvoiceBuilder` interface: `forRecipient(kind, name)`, `addSection(title, lines)` (ignores empty sections), `build()` → immutable `Invoice` (header, sections, computed total). `TextInvoiceBuilder` implements it; `Invoice.render()` prints lines with amounts and total. The director (`InvoiceController`) decides which sections apply: user → "Individual tickets" (standalone tickets of the user only; group constituents excluded) and, from C06, "Personal passes" (passes on any individual ticket of the user, including constituents); group → "Group tickets" (group tickets whose group is that group). At C02 no pass section exists (no pass objects are invented); C06 adds one director step.
3. **Singleton controllers + DI (C03).** `ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController` (+ `PassController` in C06) use `private` constructors and `getInstance()`. Because a Singleton lives for the JVM while repositories depend on the run (normal / `--fly` / tests), each controller has `configure(...)` receiving its repository and collaborator controllers; `ApplicationBootstrap.start(storage, hasher, out)` creates the repositories from `StorageLocation`, calls `configure` on every controller in dependency order (users → groups → tickets → passes → attractions → invoices → park) and returns the `CommandProcessor`. Repositories are interfaces (`UserRepository`…) with `File*Repository` implementations, so alternative implementations can be injected. Alternative (controllers constructing repositories) rejected: violates the DI requirement and couples controllers to paths.
4. **Help (C04).** `CommandRegistry` holds `CommandDefinition(name, syntax, description, parameterCount, handler)`; `CommandProcessor` dispatches through it (no switch). `HelpGenerator` receives the registry and two `HelpFormatter` strategies (concise, extended) via its constructor from the bootstrap; `HELP`/`HELP EXTENDED` call `generator.concise()` / `generator.extended()`.
5. **Passwords (C05).** `org.mindrot:jbcrypt:0.4`. `PasswordHasher` interface (`hash`, `matches`) injected into `UserController`; `BCryptPasswordHasher(logRounds)` (10 in production, 4 in tests for speed). Validation runs on the raw password before hashing. `User` stores `passwordHash`. Legacy (E1/E2) plaintext values — any stored value not starting with `$2a$`, `$2b$` or `$2y$` — are compared literally and are not migrated; they become hashes only when the password is changed. Executable JAR built with `maven-shade-plugin` so BCrypt is included.
6. **Passes (C06) — decisions on unspecified details.**
   - Syntax: `SPEEDPASS CREATE;<idTicket>;<1|10>`, `SPECIALPASS CREATE;<idTicket>`, `SPEEDPASS DELETE;<idPase>`, `SPECIALPASS DELETE;<idPase>` (the source names the commands but not their parameters).
   - IDs: SpeedPass `<idTicket>-SP<n>` with `n` = 1 + highest existing number on that ticket; Special Pass `<idTicket>-SPECIAL`.
   - Eligible tickets: any individual ticket, including constituents of group tickets (the source says passes attached to group constituents may appear on user invoices); purchase requires ticket date ≥ system date (same rule as buying tickets).
   - Refund: unused pass and ticket date strictly after the system date (E2 refund rule). A refunded Special Pass is removed and does not count toward the one-per-ticket limit (the source only says the limit holds even after use) — flagged for confirmation.
   - Model: abstract `Pass` (id, price, consumed) with `SpeedPass`, `SpecialPass`; `IndividualTicket` owns `speedPasses` (0..*) and `specialPass` (0..1) by composition, so cascades follow ownership.
   - Output: `OK SPEEDPASS CREATED -> <total> [ids]`, `OK SPECIALPASS CREATED -> <price> [id]`.
7. **Generic attraction access (C07).** Interfaces `AccessTicket` (id, date, validity, users, `getAccessUnits()` = individual tickets admitted), `IndividualAccess extends AccessTicket` (user, unused SpeedPass lookup, Special Pass) and `GroupAccess extends AccessTicket` (group). `Ticket implements AccessTicket`, `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`. `Attraction<T extends AccessTicket>` holds `Class<T> ticketKind`; `accept(AccessTicket)` returns `Optional<T>`; `enter(T, speed, today)` is a template method. Subclasses: `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (needs `IndividualAccess.getSpecialPass()`, so the bound is functional). `AttractionType` enum creates the matching subclass.
8. **Pass entry (C08).** `Attraction.enter` performs every check (kind, date, capacity, already present, required passes via overridable `requiredPasses(T, speed)`) before mutating; then admits users and consumes the returned passes. `SpecialAttraction.requiredPasses` rejects `SPEED` and requires an unused Special Pass. Failures throw `AccessDeniedException`; nothing is mutated. After success the controller persists pass state; on I/O failure it reverts users and passes.
9. **Persistence (C09).** `passes.txt`: `SPEED|SPECIAL;id;ticketId;price;consumed`, loaded after tickets and attached by ticket id. `attractions.txt`: `id;name;capacity;type` (3-field legacy lines → `GENERAL`). Cascade deletion: preflight checks first; then in-memory removal (passes, constituent tickets, group ticket); then save. `TextFiles.write` writes to a temporary sibling file and moves it into place (atomic replace), and the controller restores the previous in-memory state and rewrites files if any save fails, so no partial deletion remains.
10. **UML.** Same presentation rules as E2 (no methods; usages only for classes without incoming relationships; relationships named after fields; `x-->` at owner side); adds packages, interfaces, realization arrows, generic bounds (`Attraction<T extends AccessTicket>`, bound subclasses), passes and invoice classes.

## Risks / Trade-offs

- [Singletons hold state across tests] → `configure` fully resets state; tests call the bootstrap per scenario.
- [BCrypt cost slows tests] → injected low-cost hasher in tests.
- [Legacy plaintext passwords remain until changed] → documented; required by "no silent migration".
- [Multi-file save is not one OS transaction] → atomic per-file replace plus in-memory rollback and rewrite on failure.

## Migration Plan

Existing data files load unchanged (plaintext passwords accepted, attractions default to `GENERAL`, no `passes.txt` = no passes). Rollback: revert; files written by F3 with hashes would require password resets in E2.

## Open Questions

- Pass command parameters, pass identifiers and Special Pass refund semantics (decision 6) are interpretations of an underspecified source; confirm with course staff.
