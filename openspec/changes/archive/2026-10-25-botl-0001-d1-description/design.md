# Design

## Context

Greenfield repository (only OpenSpec scaffolding, README and LICENSE). The assignment imposes: Java 21, Maven quickstart layout, Java standard library only, no regular expressions, command parsing with `split`, separate classes for user / entry point / persistence. Later deliveries will add groups, tickets and attractions and will require a layered package structure, so the layout chosen now must accommodate growth.

## Goals / Non-Goals

**Goals:**
- One command-processing path shared by interactive and file modes.
- Validation rules reusable by both create and update.
- A package layout that later deliveries can extend without restructuring.
- Unit-testable components (persistence location injectable, I/O streams injectable).

**Non-Goals:**
- Password hashing/encryption (explicitly forbidden in this delivery).
- Concurrency, multi-process file locking.
- Reproducing the course's mandatory milestone commits (see proposal Impact).

## Decisions

### Package layout
Base package `es.upm.etsisi.park` with sub-packages:
- `app` — `App` (entry point, wiring) and `CommandRunner` (reads lines from a `BufferedReader`, delegates to the processor, stops on `EXIT`/EOF).
- `command` — `CommandProcessor` (parses a line with `split(";", -1)`, dispatches, formats results) and `CommandResult` (output text + exit flag).
- `model` — `User` (private fields, getters, setters except DNI which is `final`) and `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`).
- `validation` — `UserValidator` (pure static checks for DNI, email, password, card, non-blank).
- `service` — `UserService` (in-memory users keyed by email in a `LinkedHashMap`, uniqueness, password checks, calls repository after each change) and `UserException` (unchecked, carries the user-facing message).
- `persistence` — `UserRepository` interface and `UserFileRepository` (reads/writes the `;`-separated file).

Alternative considered: a flat package. Rejected because Delivery 3 requires layered packages and moving classes later would churn history.

### Error signalling
Business rule violations throw `UserException` with a message; `CommandProcessor` catches it and renders `ERROR: <message>`. Any unexpected `RuntimeException` during a command is also caught there and reported as `ERROR: ...`, guaranteeing invalid input never terminates the app. Alternative: return codes/booleans — rejected as it loses the reason to report.

### Persistence strategy
Whole-file rewrite after every successful mutation (`Files.write` to a temp file then `Files.move` with `REPLACE_EXISTING`), load once at start-up. The file path defaults to `users.txt` in the working directory and can be overridden with the system property `park.users.file` (used by tests and by evaluators who want isolated runs). Alternative: append-only log — rejected because delete/update would need compaction logic.

### Email lookup and update
Users are keyed by email. On an `EMAIL` update the entry is re-keyed. DNI uniqueness is checked by scanning values (small data set; O(n) is acceptable).

### Field semantics
`NAME` updates first name, `SURNAME` updates surnames. Field names are matched case-insensitively against the enum; `DNI` or any other value yields "field not modifiable".

### Input normalisation
Each element of a command is trimmed. Command keywords are case-insensitive. Data values (passwords, names) are otherwise kept verbatim. The DNI letter must be uppercase to keep a single canonical form for uniqueness.

### Output
`USER CREATED`, `USER DELETED`, `USER UPDATED` on success; `ERROR: <reason>` on failure; `HELP` prints one syntax line per command. Interactive mode prints a `> ` prompt; file mode echoes nothing but results.

### Build
`maven-jar-plugin` with `Main-Class: es.upm.etsisi.park.app.App` and `finalName` `amusement-park`, giving `target/amusement-park.jar`. JUnit Jupiter 5 (test scope) for unit tests; `maven.compiler.release=21`.

## Risks / Trade-offs

- [The assignment's examples wrap `Clave1!` across two lines (PDF line break)] → Treated as a rendering artefact; one command per physical line is the contract.
- [Plain-text passwords on disk] → Required by this delivery (encryption forbidden); Delivery 3 introduces BCrypt.
- [`;` cannot appear in field values] → Inherent to the command format; values are produced by `split` so they can never contain it.
- [Crash during write corrupts file] → Temp file + atomic move.
