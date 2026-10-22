# Design

## Context

The repository is greenfield. Apart from OpenSpec scaffolding, a README, and a Maven-oriented `.gitignore`, it has no code. Local toolchain: Maven 3.9 and a JDK newer than 21, so compilation targets `--release 21`. See proposal.md for motivation and the specs for the required behavior. Constraints from the assignment: Java standard library only at runtime, no regular expressions, command parsing with `String` operations and `split`, and separate classes for the user, the entry point, and persistence.

## Goals / Non-Goals

**Goals:**
- Small layered structure that later deliveries (park features) can extend without reworking user management.
- One command-processing path shared by interactive and file mode.
- Unit-testable components (validation, service, persistence, command processing) independent of `System.in`/`System.out`.

**Non-Goals:**
- Reproducing the assignment's mandatory Git milestone commits (`[F0-C01]` … `[F1-C09]`). Commits here are produced by the BOTL batch workflow, one per proposal, with a fixed subject format. The milestone structure is reflected in the task groups instead.
- Hashing or encrypting passwords and phone numbers (explicitly forbidden or not required in this delivery).
- Concurrency or multi-process access to the user file.

## Decisions

**Package layout (`es.upm.amusementpark`)**
- `App`: entry point only. It selects the mode from `args`, wires the components, and opens the input reader. It contains no file I/O for users.
- `model.User`: encapsulated attributes with private fields and getters. Setters exist only for modifiable fields, and `dni` is `final`.
- `model.UserField`: enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`, with a case-insensitive `fromString` that rejects unknown names (including `DNI`).
- `validation.UserValidator`: static, side-effect-free checks (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNonBlank`) built on `charAt`, `Character.isDigit/isUpperCase/...`, `indexOf`, and `lastIndexOf`.
- `persistence.UserRepository` (an interface) with `FileUserRepository`: `loadAll()` and `saveAll(Collection<User>)`. It reads lines with `split(";", -1)` and writes atomically through a temp file plus `Files.move`.
- `service.UserService`: holds the in-memory users (a `LinkedHashMap` keyed by lower-cased email) and enforces uniqueness, credentials, and validation. Each operation computes the new state, calls `saveAll`, and commits in memory only if the save succeeds. Failures are signalled with a checked `UserException` whose message becomes the `ERROR: ...` text.
- `command.CommandProcessor`: `CommandResult process(String line)` turns one line into output text and a `shouldExit` flag. `command.CommandRunner` loops over a `BufferedReader`, skipping blank lines, printing results, and stopping on `EXIT` or end of stream. Both modes use the same runner, which gives identical behavior. *Alternative considered:* separate interactive and file loops. Rejected because the spec requires one implementation.

**Why a `UserRepository` interface.** Tests can use an in-memory or failing repository to cover the write-failure scenario. Later deliveries can also swap storage without touching the service. The cost is one small interface.

**Keyword parsing.** The line is split with `split(";", -1)`, and each element is trimmed. The first element, upper-cased, is the keyword (`USER CREATE`, `HELP`, ...). Argument counts are checked exactly against the command, and a mismatch prints `ERROR: Wrong number of arguments. Usage: <syntax>`. The `-1` limit keeps trailing empty fields, so a missing last value is reported instead of silently ignored.

**Email check without regex.** Exactly one `@` (`indexOf == lastIndexOf`), non-empty local part, no whitespace. The domain has a `lastIndexOf('.')` with a non-empty name before it, an extension of length 3 after it, and no `..` or leading or trailing dot in the domain.

**Output messages.** Successes print `USER CREATED`, `USER DELETED`, and `USER UPDATED`, as in the assignment example. Errors print `ERROR: <reason>`. No prompt is printed in interactive mode, so output is identical between modes.

**Data file location.** `users.txt` is relative to the working directory. The path is injected into `FileUserRepository` so tests use temporary directories.

**JAR packaging.** The project uses `maven-jar-plugin` with `mainClass` in the manifest and `<finalName>amusement-park</finalName>`, producing `target/amusement-park.jar`, the concrete name for the assignment's `nombre-aplicacion.jar` placeholder. No shading is needed because there are no runtime dependencies.

**Testing.** JUnit Jupiter has `test` scope only, so it does not violate the runtime "no external libraries" restriction and is never packaged. The quickstart archetype itself ships a JUnit test dependency.

**Input interpretation.** The assignment's examples break `Clave1!` and `Otra2#` across two lines (`Clave1` / `!`). This is a typesetting artifact of the source document, so each command is treated as a single line.

## Risks / Trade-offs

- [Rewriting the whole file on each change is O(n)] → Acceptable for the user volumes of this delivery. The temp-file-plus-move write prevents a half-written file.
- [Passwords are stored in plain text] → Mandated by the assignment's file format and its ban on encryption. Documented as a known limitation.
- [Values cannot contain `;`] → Inherent to the format. Such input splits into extra fields and is rejected as a wrong argument count.
- [Trimming field values] → Leading or trailing spaces in passwords are not preserved. This is an accepted simplification, applied consistently in create, update, and credential checks.
