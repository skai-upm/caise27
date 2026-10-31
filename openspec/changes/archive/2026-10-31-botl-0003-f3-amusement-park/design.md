# Design

## Context

E2 baseline: flat package `es.upm.amusementpark`; `App` (modes, `--fly`), `CommandProcessor` (shared dispatcher), `ParkController` (system date, base price, cross rules) owning `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`; concrete file repositories over `TextFileStore`/`StoragePaths`; `Ticket` ← `IndividualTicket`/`GroupTicket`; plaintext passwords. See proposal.md for motivation. Constraints: Java 21, Maven, no DI framework, keep E1/E2 command syntax and file formats loadable.

## Goals / Non-Goals

**Goals:** milestone-by-milestone delivery with a passing build and the mandated commit after each; strict layer boundaries; testable singletons; atomic multi-entity operations.

**Non-Goals:** DI containers, databases, migrating legacy plaintext passwords, new commercial rules beyond the source.

## Decisions

### D1. Packages (F3-C01)
- `presentation`: `App`, `CommandProcessor`, `ApplicationContext` (composition root, F3-C03), `help.*` (F3-C04).
- `business.model`: users, groups, tickets, passes, attractions, access interfaces, enums.
- `business.controller`: `ParkController` and the managers. `business.rules`: `UserValidator`, `Checks`. `business.invoice`: invoice + builder. `business.security`: password hashing. `business.repository`: repository interfaces (F3-C03).
- `persistence`: `TextFileStore`, `StoragePaths`, `TemporaryStorage` (the `--fly` directory create/delete moved out of `App`), `File*Repository` implementations.
Presentation never touches data files: it only reads the command file it was given and asks persistence to create/remove the temporary directory.

### D2. Singleton lifecycle and injection (F3-C03, design question 2)
`ParkController`, `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager` each have a private constructor, `static initialize(dependencies…)` (rejects a second call while active), `static getInstance()` and `static reset()`. `ApplicationContext.start(StoragePaths, LocalDate, PasswordHasher)` is the only place that creates file repositories and wires controllers; `ApplicationContext.stop()` resets them when the execution ends, which lets tests simulate several executions in one JVM. Repositories are interfaces in `business.repository`, implemented in `persistence`, never Singletons; `--fly` only changes the `StoragePaths` passed to the repositories.

### D3. Invoices (F3-C02, design question 1)
`Invoice` is immutable (recipient line, sections, total). `InvoiceBuilder` is the Builder abstraction (`recipient(...)`, `section(title, lines)` — ignored when empty — and `build()` computing the total); `InvoiceService` is the director with `userInvoice(user)` / `groupInvoice(group)`. At F3-C02 the user invoice has only the direct individual-ticket section; F3-C06 adds the personal-pass section through one more builder step. No fictional pass objects exist before F3-C06.

### D4. Help (F3-C04)
`CommandRegistry` lists `CommandInfo(syntax, description)` for every command. `HelpGenerator(registry, HelpFormatter)` renders it; `SyntaxHelpFormatter` and `ExtendedHelpFormatter` are injected by `ApplicationContext` into two generators handed to `CommandProcessor`, which never constructs a formatter.

### D5. Passwords (F3-C05, design question 5)
`PasswordHasher` interface (`hash`, `matches`) with `BCryptPasswordHasher(cost)` using `org.mindrot:jbcrypt:0.4`; injected into `UserManager`. Production cost 10; tests inject cost 4 for speed. `matches` treats values not starting with `$2` as legacy E1/E2 plaintext (compared directly) — no silent migration; a legacy user gets a hash when the password is next changed. The JAR is built with `maven-shade-plugin` so `java -jar` includes jbcrypt.

### D6. Passes (F3-C06, design question 3)
The source fixes behavior but not command arguments or identifiers; chosen (assumption, recorded here):
- `SPEEDPASS CREATE;<ticketId>;<1|10>`, `SPECIALPASS CREATE;<ticketId>`, `SPEEDPASS DELETE;<passId>`, `SPECIALPASS DELETE;<passId>`.
- Ids `<ticketId>-SPEED<n>` (n = highest used + 1 for that ticket) and `<ticketId>-SPECIAL`.
- Passes attach to individual tickets only (stand-alone or constituents, which group SPEED entry needs); not to group tickets.
- Purchase requires the ticket date ≥ system date (mirrors E2 ticket purchase); refund requires date > system date (E2 refund rule) and an unused pass.
- "At most one Special Pass throughout its lifetime" is literal: a `specialPassIssued` flag survives consumption and refund.
- Output `OK SPEEDPASS CREATED -> <total>` / `OK SPECIALPASS CREATED -> <price>` mirrors E2 purchase output.
Model: abstract `Pass` (id, price, consumed) ← `SpeedPass`, `SpecialPass`; `IndividualTicket` composes `List<SpeedPass>` and an optional `SpecialPass`.

### D7. Access interfaces and generics (F3-C07, design question 6)
`AccessTicket` (capability to enter: `getId`, `isValidOn`, `getAccessTickets()` = individual tickets whose users enter) ← `IndividualAccess` (implemented by `IndividualTicket`) and `GroupAccess` (implemented by `GroupTicket`). `Attraction<T extends AccessTicket>` keeps `Class<T> admitted`; `admit(AccessTicket)` casts through it (rejecting others) and runs `enter(T ticket, …)`. Subclasses: `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (overrides the pass step). The bound is what decides admissibility, so it is functional. `AttractionType` enum names the kinds and acts as factory key. Optional fifth parameter of `ATTRACTION CREATE` gives the type (assumption: appended after capacity).

### D8. Pass-based entry (F3-C08)
`Attraction.enter` runs every check (type, date, presence, capacity, pass availability) before any mutation, then admits users and consumes passes. Pass rule per type: SPEED allowed at GENERAL/INDIVIDUAL for individual and GENERAL/GROUP for group tickets (by construction of the admitted type); `SpecialAttraction` rejects SPEED and requires/consumes the Special Pass. The returned list of consumed passes is reported by the command.

### D9. Persistence consistency (F3-C09, design question 4)
- Files: `passes.txt` = `passId;kind;ticketId;price;consumed`; `tickets.txt` gains optional 9th column `specialPassIssued`; `attractions.txt` gains optional 4th column `type`. Old 8/3-column lines still load.
- `TextFileStore.writeAll(map)` stages every file of a save to `*.tmp` siblings first; only if all succeed are they moved into place (`ATOMIC_MOVE` where supported). A failure during staging deletes the temporaries and leaves all previous files untouched; managers additionally restore in-memory state.
- Deletions run all preflight checks before mutating. Ticket/group-ticket deletion removes the aggregate (passes → constituents → group ticket in that logical order) and persists tickets, group tickets and passes in one staged write.

### D10. Milestone commits
Each milestone ends with `mvn test` green and a local commit with the exact source message (only reviewed code/test/doc paths). OpenSpec artifacts, the proposal copy and archived change are committed afterwards in the BOTL archive commit; all commits are pushed together.

## Risks / Trade-offs

- [Static singletons complicate tests] → explicit `reset()` via `ApplicationContext.stop()` in test teardown.
- [Two-phase file move is not crash-proof across files] → acceptable for a file-based app; staging removes the common failure (write errors) without partial state.
- [Legacy plaintext passwords remain until changed] → documented; explicitly required not to migrate silently.
- [Pass command syntax is an assumption] → listed in D6 and in `HELP EXTENDED` for visibility.
- [BCrypt slows tests] → injectable cost.

## Migration Plan

E2 data files load unchanged (missing optional columns default to GENERAL / not issued; missing `passes.txt` = no passes). Rollback = revert the change; files written by F3 with extra columns are not readable by E2.
