# Design

## Context

E2 baseline: one package `es.upm.amusementpark` with `App`, `CommandProcessor`, `Park` (controller holding date/price, groups, tickets, attractions), `UserManager`, domain classes, and four file repositories selected through `StorageConfig` (normal vs `--fly`). See proposal.md for motivation and specs/ for behaviour. Archived E1/E2 specs are the authority for earlier rules (E2 refund rule: ticket date strictly after system date; purchase not before system date).

## Goals / Non-Goals

**Goals:** implement F3-C01…C10 in order, each leaving `mvn test` green; keep every E1/E2 command and output unchanged unless F3 changes it; keep state changes all-or-nothing in memory and on disk.

**Non-Goals:** DI frameworks, databases, migrating stored plaintext passwords, new pricing/access policies beyond the source.

## Decisions

### Packages (F3-C01)
```
es.upm.amusementpark.presentation          App (entry + composition root), CommandProcessor, CommandResult
es.upm.amusementpark.presentation.help     CommandInfo, CommandCatalog, HelpGenerator, HelpFormatter (+ Concise/Extended)
es.upm.amusementpark.business.model        User, UserField, Group, Ticket, IndividualTicket, GroupTicket, Attraction…, passes, access interfaces
es.upm.amusementpark.business.controller   SystemController, UserController, ParkController, InvoiceController
es.upm.amusementpark.business.invoice      Invoice, InvoiceSection, InvoiceLine, InvoiceBuilder, StandardInvoiceBuilder
es.upm.amusementpark.business.repository   UserRepository, GroupRepository, TicketRepository, AttractionRepository (interfaces)
es.upm.amusementpark.business.security     PasswordHasher, BCryptPasswordHasher
es.upm.amusementpark.business              OperationException, UserValidator
es.upm.amusementpark.persistence           File*Repository, StorageConfig, TextFiles (atomic writes)
```
C01 is a pure move (classes keep names; repositories stay concrete). Repository interfaces arrive in C03. `App` is the composition root: it chooses `StorageConfig` and constructs repositories but never reads/writes data files itself (only persistence classes do).

### Controllers and dependency injection (F3-C03)
- Unique controllers: `SystemController` (system date, base price), `UserController` (ex-`UserManager`: users, validation, authentication), `ParkController` (ex-`Park`: groups, tickets, passes, attractions, cross-entity rules), `InvoiceController`.
- Singleton form: private constructor, `static synchronized initialize(deps…)` creating the single instance (second call → `IllegalStateException`), `static getInstance()`, and `static synchronized release()` called by `App` when an execution ends (tests run several executions in one JVM). Alternative — eager static instances — rejected: dependencies are only known at start-up (normal vs fly paths).
- Repositories are interfaces in `business.repository`, implemented by `File*Repository` in `persistence`; `App` builds them from `StorageConfig` and passes them to `initialize`. No repository is a Singleton, so fly and normal runs (and tests with in-memory fakes) only differ in what is injected.
- Collaborators injected at start-up: `ParkController(SystemController, UserController, repos)`, `UserController(UserRepository, PasswordHasher)`, `InvoiceController(UserController, ParkController, Supplier<InvoiceBuilder>)`.

### Invoices with Builder (F3-C02, completed in F3-C06)
- `InvoiceBuilder` interface: `forRecipient(String)`, `addSection(String title, List<InvoiceLine> lines)` (ignored when empty), `build()` (computes total). `StandardInvoiceBuilder` implements it; `Invoice` is immutable (recipient, sections, total). `InvoiceController` acts as director with distinct steps.
- User invoice: section "Individual tickets" = top-level `IndividualTicket`s whose holder is the user; constituent tickets are never listed as purchases. In C02 only this section exists. **Sequencing:** C06 adds the "Passes" section (passes on any of the user's individual tickets, including constituents) as one more builder step — no placeholder pass objects exist before C06.
- Group invoice: section "Group tickets" = group tickets whose `group` is that group (same object), price each; no constituents or passes.
- Presentation formats the `Invoice` as text (`INVOICE - …`, one line per item with price, `TOTAL: x.xx`). Invoices are never stored.

### Help (F3-C04)
`CommandCatalog` holds the registered `CommandInfo(syntax, description)` list. `HelpGenerator(List<CommandInfo>, HelpFormatter)` builds text by delegating to the injected `HelpFormatter`. `App` creates two generators (concise, extended) and injects them into `CommandProcessor`, which maps `HELP` / `HELP EXTENDED` to them and never constructs formatters.

### Passwords (F3-C05)
- Dependency `org.mindrot:jbcrypt:0.4`; `maven-shade-plugin` bundles it into `target/parque.jar`.
- `PasswordHasher` (`hash`, `matches`) injected into `UserController`; `BCryptPasswordHasher(logRounds)` — 10 in the application, 4 in tests for speed.
- Format validation runs on the plaintext before hashing (create and `PASSWORD` update).
- **E1/E2 compatibility:** stored values not starting with `$2a$`/`$2b$`/`$2y$` are legacy plaintext; `matches` compares them exactly. They are not rewritten silently; they are replaced by a hash only when the user changes the password.

### Passes (F3-C06)
- `Pass` (abstract: `id`, `price`, `consumed`) with `SpeedPass` and `SpecialPass`. `IndividualTicket` owns them by composition: `speedPasses: List<SpeedPass>`, `specialPass: SpecialPass` (nullable) and a `specialPassIssued` flag (lifetime rule survives consumption and refund).
- **Command arguments (not defined by the source; recorded decision):** `SPEEDPASS CREATE;<IdTicket>;<1|10>`, `SPECIALPASS CREATE;<IdTicket>`, `SPEEDPASS DELETE;<IdPase>`, `SPECIALPASS DELETE;<IdPase>`. `IdTicket` may be a direct or constituent individual ticket.
- Identifiers: `<IdTicket>-SP<n>` with `n` = 1 + highest SpeedPass number ever used on that ticket (derived from existing ids, consumed passes are kept so numbers are not reused except after refunds of the highest), `<IdTicket>-SPECIAL`.
- **Date rules (reusing E2):** purchase allowed when the ticket is not in the past (date ≥ system date, as for ticket purchase); refund only for unused passes whose ticket date is strictly after the system date.
- Prices: SpeedPass = ticket price × 0.20 (scale 2, HALF_UP); Special Pass = ticket price. Output: `OK SPEEDPASS CREATED -> <total> [ids]`, `OK SPECIALPASS CREATED -> <price> [id]`.

### Attraction typing with interfaces and generics (F3-C07)
- Access interfaces: `Admission` (`getId`, `getDate`, `getUsers`, `getIndividualAdmissions`) implemented by `Ticket`; `IndividualAdmission extends Admission` (`getUser`, `findUnusedSpeedPass`, `findUnusedSpecialPass`) implemented by `IndividualTicket`; `GroupAdmission extends Admission` implemented by `GroupTicket`.
- `Attraction<T extends Admission>` (abstract) keeps `Class<T> admitted`, capacity, visitors; `admit(Admission, boolean speed)` checks `admitted.isInstance(...)` (type rule) and calls the typed `enter(T, boolean speed)`. Subclasses: `GeneralAttraction extends Attraction<Admission>`, `IndividualAttraction extends Attraction<IndividualAdmission>`, `GroupAttraction extends Attraction<GroupAdmission>`, `SpecialAttraction extends Attraction<IndividualAdmission>` (overrides the pass rule). The bound is functional: it is the compile-time type of `enter` and the runtime admission filter.
- `AttractionType` enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) with a factory method creating the matching subclass. Creation syntax: optional 5th parameter.
- Constituent tickets become addressable for ENTER/EXIT as individual admissions (needed to use a constituent's Special Pass); top-level lookup is tried first.

### Pass-based entry (F3-C08)
`Attraction.enter` validates everything first (date checked by the controller; type; capacity; duplicates; pass availability for every individual admission when `speed`, or Special Pass for `SPECIAL`), then mutates visitors and marks passes consumed, returning consumed pass ids for the output (`OK: n user(s) entered attraction A01 (consumed: T001-SP1)`). `SPECIAL` rejects `speed`. `INDIVIDUAL`/`GENERAL` accept speed for individual tickets, `GROUP`/`GENERAL` for group tickets. The controller persists tickets after a consuming entry; if that save fails the entry is undone (visitors removed, passes un-consumed).

### Persistence and UML (F3-C09)
- `tickets.txt`: `I`/`C` lines gain a 9th field `specialPassIssued` (`true|false`; 8-field lines load as `false`); each `I`/`C` line is followed by its passes: `S;id;price;consumed` (SpeedPass) and `X;id;price;consumed` (Special Pass).
- `attractions.txt`: `id;name;capacity;TYPE` (3-field lines → `GENERAL`).
- **Consistency:** controllers validate all preconditions before mutating; `TextFiles.writeAtomically` writes `<file>.tmp` then `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`, so a failed save never leaves a partial file, and the in-memory change is undone. Cascades are structural: removing a ticket removes the passes it owns; removing a group ticket removes its constituents (and their passes) — one save of the tickets file.
- `--fly`: passes and types live in the same repository files, so the existing path switch isolates them.
- `uml/model.puml` regenerated from the F3 code with the E2 presentation rules, now with packages, interfaces, and generic bounds.

### Commit strategy
The source mandates ten milestone commits with exact messages. They are created during apply, in order, each only after `mvn test` passes for that milestone, on the BOTL target branch; the BOTL orchestration commit (archive, archived proposal copy, trailers) follows the archive phase, and all are pushed together.

## Risks / Trade-offs

- [Legacy plaintext passwords remain verifiable] → Explicit compatibility decision; they disappear as users change passwords. Documented here rather than silently migrated.
- [Singletons complicate tests] → `release()` per execution; tests initialise and release around each case.
- [Pass command syntax is a recorded interpretation] → Listed in `HELP EXTENDED`; easy to adapt if the authoritative syntax differs.
- [Shaded JAR] → Slightly larger artifact; no runtime classpath setup needed.

## Migration Plan

Existing `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt` load unchanged (new fields default). Files are rewritten in the new format on the next save.
