# Design

## Context

The repository contains only OpenSpec scaffolding (no source code). Delivery 1 fixes the technology (Java 21, Maven quickstart layout, executable JAR) and forbids regular expressions, external libraries, frameworks, databases and encryption in the application. See proposal.md for motivation and the specs for the observable behaviour.

## Goals / Non-Goals

**Goals:**
- A small layered structure that later deliveries (attractions, tickets, groups) can extend without rewriting command handling or persistence.
- One command-processing path shared by interactive and file modes.
- Fully unit-testable domain, validation, persistence and command layers.

**Non-Goals:**
- Reproducing the assignment's eleven mandatory milestone commits (`[F0-C01]` … `[F1-C09]`). This change is delivered through the BOTL batch workflow, which produces exactly one commit per proposal; the milestone history is a process requirement of the course, not application behaviour, and is recorded here as a known deviation.
- Password hashing (explicitly excluded in Delivery 1).

## Decisions

### Package layout (`es.upm.park`)
- `App` — entry point: chooses interactive or file mode, wires components, contains no parsing or file I/O logic for users.
- `model.User` — encapsulated user; private fields, getters, setters for every field except DNI (`final`, no setter).
- `model.UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD` with a lenient `parse(String)`.
- `validation.UserValidator` — static, side-effect-free checks (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`) built only from `String`/`char` operations and `split`.
- `persistence.UserFileRepository` — loads/saves the users file (`Path` injected; default `users.txt` in the working directory).
- `service.UserService` — create/delete/update rules, uniqueness, credential checks; throws `UserOperationException` with a user-facing message; calls the repository after each successful mutation.
- `cli.CommandProcessor` — parses one line (`split(";", -1)`, trim), dispatches, returns a `CommandResult(output, exit)`.
- `cli.CommandRunner` — reads lines from any `BufferedReader` (stdin or file), skips blank lines, prints output, stops on `exit`.

Alternative considered: putting parsing in `App`. Rejected because the assignment explicitly separates the main class from logic and because both input modes must share one implementation.

### Error reporting
Errors are printed as `ERROR: <message>` to standard output (so they appear in the same stream and order as success messages, which is what command-file graders see). No exception escapes `CommandProcessor`; unexpected runtime exceptions are caught there and reported as errors.

### Persistence strategy
The whole file is rewritten after every successful mutation (write to a sibling temp file, then `Files.move` with `REPLACE_EXISTING`/`ATOMIC_MOVE` fallback). Data volumes are tiny, and this keeps the file always consistent. Malformed lines on load are skipped with a warning on standard error.

### Parsing details
- Command name = first field, upper-cased and with internal whitespace collapsed, so `user  create` works.
- Field count must match exactly; otherwise the expected syntax is printed.
- `split(";", -1)` keeps trailing empty fields so missing values are detected as empty rather than silently dropped.
- The proposal's examples show `Clave1` / `!` wrapped across two lines; that is a document line-wrap artifact, and each command is one physical line.

### Email rules
Exactly one `@`; local part non-empty; domain part split on `.` with every label non-empty, at least two labels, and the last label exactly three letters. No whitespace anywhere. Uniqueness compares emails ignoring case.

### Testing
JUnit Jupiter 5.11.4 at `test` scope only (the delivered JAR has no runtime dependencies). `maven-jar-plugin` sets `Main-Class` and `finalName` = `amusement-park`.

## Risks / Trade-offs

- [Plain-text passwords in `users.txt`] → Required by Delivery 1 ("no encryption"); a later delivery is expected to introduce hashing.
- [`;` cannot appear inside a value] → Inherent to the command and file formats; any value containing `;` is split into extra fields and rejected by the field-count check.
- [Data file location depends on working directory] → Documented; `App` accepts a `park.users.file` system property to override it (also used by integration tests).
