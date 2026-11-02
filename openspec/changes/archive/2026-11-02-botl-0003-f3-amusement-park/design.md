# Design

## Context

E2 baseline: one package `com.amusementpark` with `App`, `CommandProcessor` (operation table), `ParkController` (system date, base price, cross-manager rules), `UserManager`/`GroupManager`/`TicketManager`/`AttractionManager`, domain classes, `LineFile` plus four `*FileStorage` classes and `StoragePaths`. Passwords are stored in plaintext. Archived E2 design decisions (exit by ticket or email, constituent ids `<id>-<n>`, holder snapshots, `--fly` cleanup) stay in force. See `proposal.md` for scope and `specs/` for behavior.

## Goals / Non-Goals

**Goals:** implement F3-C01…C10 in order, each milestone buildable and tested and committed with its mandated message; keep E1/E2 command syntax and outputs; all-or-nothing state changes.

**Non-Goals:** DI framework, database, migration of plaintext password files, new access policies beyond the source.

## Decisions

### D1. Package layout (F3-C01)
- `presentation`: `App` (entry point and composition root), `CommandProcessor`, `CommandDefinition`, help classes.
- `business.model`: users, groups, tickets, passes, attractions, access interfaces, `AttractionType`, `UserField`, `UserValidator`.
- `business.controller`: `ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController`.
- `business.invoice`: `Invoice`, `InvoiceBuilder`, `TextInvoiceBuilder`.
- `business.security`: `PasswordHasher`, `BCryptPasswordHasher`.
- `persistence`: `LineFile`, `StoragePaths`, repository interfaces and `*FileRepository` implementations.

Persistence implementations depend on the business model (they map lines to objects); business controllers depend only on the repository *interfaces*. Repository interfaces live in `persistence`, as the source places repositories there. *Alternative:* interfaces in business (dependency inversion) — cleaner dependency direction but contradicts the literal layer assignment.

### D2. Invoice Builder and sequencing (F3-C02, F3-C06)
`InvoiceBuilder` declares steps `start(title, date)`, `addTickets(List<IndividualTicket>)`, `addGroupTickets(List<GroupTicket>)`, `addPasses(List<Pass>)`, `build()`; each `add*` ignores empty lists so only applicable sections appear. `TextInvoiceBuilder` produces an immutable `Invoice` (title, sections of `description → amount` lines, total) rendered as text. `InvoiceController` is the director: user invoice = `start` + standalone tickets + passes; group invoice = `start` + group tickets. At F3-C02 the pass step does not exist; F3-C06 adds `addPasses` and the user-invoice call — no placeholder pass objects are created earlier. A fresh builder is obtained from an injected `Supplier<InvoiceBuilder>` per invoice.

### D3. Singletons and injection (F3-C03)
Each controller has a private constructor, a static `getInstance()` returning its single instance, and `configure(...)` receiving repositories and collaborator controllers and loading state. `App`'s composition root (`App.configure(StoragePaths, PasswordHasher, LocalDate)`) creates the file repositories for the chosen paths, configures controllers in dependency order, and returns the park controller. Re-running `configure` starts a new execution (used by tests and by consecutive in-process runs); the instance stays the same. Repositories are plain objects created per run, so normal and `--fly` runs differ only in `StoragePaths`. *Alternative:* `initialize()` that replaces the instance — rejected: two instances could then coexist in references.

### D4. Help (F3-C04)
`CommandProcessor` builds a registry of `CommandDefinition(name, minParams, maxParams, syntax, description, action)`. `HelpGenerator` takes a `HelpFormatter` (strategy) in its constructor and formats `HelpEntry(syntax, description)` items; `App` injects one generator with `SyntaxHelpFormatter` for `HELP` and one with `ExtendedHelpFormatter` for `HELP EXTENDED`. Neither the processor nor the generator constructs a formatter.

### D5. BCrypt (F3-C05)
Dependency `org.mindrot:jbcrypt:0.4`. `BCryptPasswordHasher(logRounds)` implements `PasswordHasher` (`hash`, `matches`); production uses 10 rounds, tests inject 4 for speed. `UserController` validates the E1 format first, then hashes on create and on `PASSWORD` update; `authenticate` uses `matches`. `maven-shade-plugin` bundles jBCrypt so `target/parque.jar` stays runnable with `java -jar`.

### D6. Existing plaintext password files (open question 5)
No migration is performed. `matches` treats a stored value without a BCrypt prefix (`$2a$`, `$2b$`, `$2y$`) as a legacy E1/E2 plaintext value and compares it literally; it is replaced by a hash only when the user changes the password. All passwords written by F3 code are hashes. **Flag for review:** legacy plaintext values remain until changed; an explicit migration command can be added if wanted.

### D7. Passes and command syntax (open question 3)
The source names the commands but not their arguments. Chosen syntax, consistent with existing commands and recorded as an assumption:
- `SPEEDPASS CREATE;<idEntrada>;<1|10>`, `SPEEDPASS DELETE;<idPase>`
- `SPECIALPASS CREATE;<idEntrada>`, `SPECIALPASS DELETE;<idPase>`
Pass identifiers are generated: `<idEntrada>-S<n>` (numbered after the highest existing number of that ticket) and `<idEntrada>-SPECIAL`. Outputs mirror ticket purchases: `OK SPEEDPASS CREATED -> <total>`, `OK SPECIALPASS CREATED -> <price>`. Passes apply to any individual ticket, including group constituents (needed for group SPEED entry). Purchase requires the ticket dated on or after the system date (same rule as buying tickets); refund requires it strictly later (E2 refund rule). SpeedPass price = ticket price × 0.20 rounded half-up.

`IndividualTicket` owns its passes by composition (`speedPasses`, `specialPass`) and a `specialPassPurchased` flag that enforces "one Special Pass in the ticket's lifetime" after a refund. `Pass` (abstract: `id`, `price`, `consumed`) has subclasses `SpeedPass` and `SpecialPass`; passes hold no back-reference to their ticket.

### D8. Access interfaces and bounded generics (F3-C07, F3-C08)
- `Admission`: `getId()`, `getDate()`, `isValidOn(date)`, `getAdmissions(): List<IndividualAdmission>`.
- `IndividualAdmission extends Admission`: `getUser()`, `nextUnusedSpeedPass()`, `getSpecialPass()`.
- `GroupAdmission extends Admission`: `getTickets()`.

`Ticket implements Admission`; `IndividualTicket implements IndividualAdmission`; `GroupTicket implements GroupAdmission`. `Attraction<T extends Admission>` stores `Class<T> admittedType`; `enter(Admission, date, speed)` rejects tickets that are not `T` and otherwise runs `enterTyped(T, …)`, which checks date, presence, and capacity and calls the overridable `selectPasses(T, speed)` before changing anything. Subclasses: `GeneralAttraction extends Attraction<Admission>`, `IndividualAttraction extends Attraction<IndividualAdmission>`, `GroupAttraction extends Attraction<GroupAdmission>`, `SpecialAttraction extends Attraction<IndividualAdmission>` (overrides `selectPasses` to require the unused Special Pass and reject SPEED, using `IndividualAdmission` methods that only the bound guarantees). Default `selectPasses` with SPEED picks one unused SpeedPass per admission and fails if any is missing. Passes are consumed and visitors added only after every check passes. `AttractionType` creates the right subclass. `ATTRACTION CREATE` takes an optional 4th field type (default `GENERAL`). `ATTRACTION ENTER` takes an optional 3rd field that must be `SPEED`.

### D9. Persistence (F3-C09)
- `passes.txt`: `id;kind;ticketId;price;consumed` (kind `SPEED`/`SPECIAL`).
- `tickets.txt`: E2 8 fields + `specialPassPurchased` (9th); 8-field lines load with `false`.
- `attractions.txt`: E2 3 fields + type; 3-field lines load as `GENERAL`.
`LineFile.read` accepts a set of allowed field counts. The ticket repository writes passes, then tickets, then group tickets. Deletions run preflight checks first, then mutate memory, then save; on `IOException` the previous lists and pass states are restored and all three files rewritten, so no partial deletion persists. Pass consumption on entry is persisted the same way; on save failure the entry is undone (visitors removed, passes released).

### D10. Milestone commits
After each milestone's tests pass, a local commit with the exact mandated message is created containing the code (`pom.xml`, `src/`, `uml/`, `.gitignore`). OpenSpec artifacts, synced specs, and the archived input proposal go into the final batch commit after archive; nothing is pushed before that commit exists.

## Risks / Trade-offs

- [Legacy plaintext passwords keep working] → documented flag (D6); new and changed passwords are hashed.
- [Singletons with re-configuration share state across in-process runs] → `configure` fully reloads state; tests always configure before use.
- [Unspecified pass syntax may differ from an external reference] → isolated in the command registry; changing syntax affects only `CommandProcessor` and help.
- [Shade plugin enlarges the JAR] → acceptable; required for a standalone JAR with BCrypt.
