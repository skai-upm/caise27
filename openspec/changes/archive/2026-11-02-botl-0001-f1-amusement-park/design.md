# Design

## Context

Greenfield repository: no `pom.xml`, no Java sources, no existing OpenSpec specs. JDK 21+ and Maven 3.9 are available locally. The motivation and scope are in proposal.md; the behaviour contract is in `specs/`. Delivery 1 forbids databases, frameworks, external runtime libraries, regular expressions and encryption, and requires command parsing with basic String operations and `split`.

## Goals / Non-Goals

**Goals:**
- One command-processing pipeline shared by interactive and file mode.
- Validation logic reusable by both creation and update.
- Every rejected command leaves memory and the users file untouched.
- High automated test coverage of every spec scenario (JUnit, test scope only).

**Non-Goals:**
- Features from later deliveries (attractions, tickets, etc.).
- Phone number validation or any encryption/hashing of passwords (explicitly excluded in Delivery 1).
- Localised or exact output wording beyond the illustrative `USER CREATED` / `USER UPDATED` / `USER DELETED` messages.

## Decisions

### D1. Package and class layout
Base package `es.upm.etsisi.amusementpark`:
- `App` — entry point only: chooses input source from `args`, wires collaborators, prints file-open errors. No user file I/O.
- `user.User` — encapsulated entity; `dni` is `final` with no setter.
- `user.UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD` with a lookup that returns empty for unknown names (so `DNI` is rejected).
- `user.UserValidator` — static checks for DNI, email, password, card; implemented with `charAt`, `Character.isDigit/isUpperCase/...`, `indexOf`, `split`. No `java.util.regex`, no `String.matches`.
- `user.UserService` — business operations create/delete/update over an in-memory list, enforcing validation and uniqueness, delegating persistence to a `UserRepository`.
- `persistence.UserRepository` (interface) + `persistence.UserFileRepository` — load/save the `;`-separated text file.
- `command.CommandProcessor` — parses one line (trim, `split(";", -1)`) and dispatches to `UserService`; returns a result with output text and an exit flag.
- `command.CommandRunner` — reads lines from any `BufferedReader` (stdin or file), skips blank lines, stops on EXIT or end of input.

*Alternative considered:* a single `Main` doing parsing and I/O — rejected, violates the separation-of-responsibilities requirement.

### D2. One runner for both input modes
`CommandRunner.run(BufferedReader, PrintStream)` is the only loop. `App` wraps either `System.in` or `Files.newBufferedReader(path)`. This guarantees identical behaviour (spec: Identical processing for both sources). Blank lines are ignored in both modes (harmless in interactive mode and keeps the pipeline identical).

### D3. Atomic mutations with rollback on save failure
`UserService` validates everything first, then mutates a copy-on-write list: it builds the new list, asks the repository to save it, and only on success replaces the in-memory list. Updates apply to a cloned `User`. A save failure is reported as an error and leaves memory unchanged. The file repository writes to a sibling temporary file and moves it over the target (`Files.move` with `REPLACE_EXISTING`, `ATOMIC_MOVE` attempted first) so a crash never leaves a half-written users file.

### D4. Users file location
The source does not name the file. Default: `users.txt` in the working directory. `App` passes this path to `UserFileRepository`; tests inject temporary paths. Malformed lines found while loading (wrong field count) are skipped with a warning on standard error rather than aborting start-up.

### D5. Parsing conventions
- Commands are matched case-sensitively on the first `;`-separated token (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), after trimming the line.
- Exact field counts are required: CREATE 8 tokens, DELETE 3, UPDATE 5, HELP/EXIT 1. `split(";", -1)` is used so trailing empty fields are counted.
- Data fields are taken as-is (no trimming inside fields) so values round-trip exactly through the users file.

### D6. Validation details (interpretation of source rules)
- DNI: length 9, first 8 `Character.isDigit` (ASCII `0`-`9`), last char equal to `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23)`. Uppercase letter required (lowercase rejected).
- Email: exactly one `@`; local part non-empty; domain part contains a `.`; text before the last `.` non-empty; text after the last `.` exactly 3 characters, all letters. So `ana@correo.com` is valid; `ana@correo.es`, `ana@.com`, `@correo.com`, `ana@correo`, `ana` are invalid.
- Password: at least one uppercase, one lowercase, one digit, and one symbol = any char that is not a letter, digit, or whitespace. No minimum length is specified, so none is imposed.
- Card: length 16, all ASCII digits.
- Name, surnames and phone: no validation rules are specified in Delivery 1; they are stored as given.

### D7. Output messages
Success: `USER CREATED`, `USER DELETED`, `USER UPDATED`. Errors: a single line starting with `ERROR:` and a short reason. HELP prints the five command syntaxes. EXIT prints `BYE`.

### D8. Build
`pom.xml` from the Quickstart archetype (`maven.compiler.release` 21, JUnit Jupiter test scope), with `<finalName>nombre-aplicacion</finalName>` and `maven-jar-plugin` manifest `mainClass` = `es.upm.etsisi.amusementpark.App`. No shading needed since there are no runtime dependencies.

## Risks / Trade-offs

- [Source example lines break `Clave1` / `!` across two lines (PDF artefact)] → Treated as the single value `Clave1!`; a literally split line would be two malformed commands and is rejected safely.
- [Prescribed Git history: the source mandates 11 separate commits with exact messages `[F0-C01] …` to `[F1-C09] …`] → This change runs under the BOTL batch harness, which owns Git history and records exactly one commit per proposal. The 11 milestone commits are therefore **not** produced by this change; implementation still proceeds in checkpoint order (task groups 1–11 map to the milestones) and each group is verified before the next. Flagged here rather than silently resolved.
- [Plain-text passwords in `users.txt`] → Required by the source (encryption is prohibited in Delivery 1).
- [Atomic move unsupported on some filesystems] → Fallback to a non-atomic replace; still written from a complete temp file.
- [Sonar/static-analysis noise from `System.out`] → Output is written to an injected `PrintStream`; only `App` binds it to `System.out`.

## Open Questions

- Exact message wording is free per the source ("exact output messages may vary"); chosen wording can change without affecting specs.
