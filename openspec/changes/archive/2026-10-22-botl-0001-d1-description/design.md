# Design

## Context

The repository is empty apart from OpenSpec tooling, so this change is greenfield. See proposal.md for motivation and specs/ for the behavioral contract. Hard constraints from the assignment: Java 21, Maven quickstart layout, only the Java standard library at runtime, no regular expressions for parsing/validation, no databases or persistence frameworks, and separate classes for the user, the entry point and persistence.

## Goals / Non-Goals

**Goals:**
- A layered structure that later deliveries (attractions, tickets, groups) can extend without reorganizing.
- A single command-processing path shared by interactive and file mode.
- Unit-testable components (validation, service, persistence, command processing) with no dependency on `System.in`/`System.out`.

**Non-Goals:**
- Design patterns beyond plain separation of responsibilities (not required in this delivery).
- Encryption or hashing of passwords (explicitly forbidden in this delivery).
- Reproducing the course's mandatory `[F0-C01]`…`[F1-C09]` student commit roadmap; repository commits are produced by the batch workflow.

## Decisions

**Package layout (`es.upm.etsisi.park`)**
- `App` — entry point only: wires objects and chooses the input source.
- `model.User`, `model.UserField` — encapsulated user (private fields, no DNI setter) and the enum of updatable fields.
- `validation.UserValidator` — static, side-effect-free checks for DNI, email, password, card and non-empty text; reused by create and update.
- `persistence.UserFileStore` — reads/writes the `;`-separated text file; knows nothing about commands.
- `repository.UserRepository` — in-memory list of users backed by `UserFileStore`; saves after each mutation; lookups by DNI and email (email case-insensitive).
- `service.UserService` — business rules (validation, uniqueness, credential check, update by field); signals failures with `UserOperationException` (unchecked) carrying a user-facing message.
- `cli.CommandProcessor` — parses one line with `split(";", -1)`, dispatches, prints results/errors to a `PrintStream`, returns whether processing should continue.
- `cli.CommandRunner` — reads lines from any `BufferedReader`, skips blank lines, stops on `EXIT` or end of input. Interactive and file mode only differ in the reader passed in.

Alternative considered: a single `Main` with a switch on commands — rejected because the assignment requires persistence and parsing to live outside the main class, and later deliveries add many commands.

**Command name matching.** The first `;`-separated element is trimmed and compared case-insensitively against `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`. Arguments are trimmed. A wrong argument count prints the expected syntax.

**Persistence strategy.** The whole file is rewritten after every successful mutation (small data set, simplest consistent approach). Missing file → empty list; malformed lines (≠ 7 fields) are skipped with a warning on stderr. File location: `users.txt` in the working directory; the store takes a `Path` so tests use temporary directories.

**Validation details.** Email: exactly one `@` (checked with `split("@", -1)`), local part non-empty, domain split at its last `.` into a non-empty name and a 3-letter extension, no whitespace. DNI: 8 digits + letter, normalized to uppercase, check letter `TRWAGMYFPDXBNJZSQVHLCKE[n % 23]`. Password symbol = any character that is not a letter, digit or whitespace. All implemented with `Character` methods and loops, no regex.

**Output messages.** Successes print `USER CREATED`, `USER DELETED`, `USER UPDATED`; failures print `ERROR: <reason>`. Exact texts are not mandated by the assignment.

**Build.** `maven-jar-plugin` sets `Main-Class`; `finalName` is `amusement-park`, giving `target/amusement-park.jar`. `maven.compiler.release=21`. JUnit 5 is a `test`-scoped dependency only, so the JAR needs no external libraries.

## Risks / Trade-offs

- [The assignment's example commands are line-wrapped (`Clave1` / `!`)] → Treated as a document formatting artefact: the intended password is `Clave1!`, one command per line.
- [Whole-file rewrite is not atomic] → Write to a temporary file in the same directory and move it over the target.
- [`;` inside a value would corrupt the file] → Impossible by construction: values come from `;`-split commands.
- [Plain-text passwords on disk] → Mandated by this delivery (no encryption); a later delivery introduces hashing.
