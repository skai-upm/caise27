# Design

## Context

The repository has no source code yet. See proposal.md for the Delivery 1 scope. The constraints are Java 21 and Maven (quickstart layout), the standard library only at runtime, no regular expressions, command parsing with String operations and `split`, file-based persistence, and separate classes for the user, the main class and persistence.

## Goals / Non-Goals

**Goals:**
- One command-processing path shared by interactive and file modes.
- Validation rules in one reusable place that both creation and update call.
- Classes small enough to extend in Delivery 2 and 3 without rewrites.

**Non-Goals:**
- Delivery 2/3 features (dates, tickets, attractions, BCrypt, etc.).
- Phone format validation (not specified for Delivery 1).

## Decisions

**Package and classes** (`es.upm.amusementpark`):
- `App`: entry point. It picks the mode from `args`, builds the collaborators, and has no file or parsing logic.
- `User`: an encapsulated entity. Every field is private, `dni` is `final` with no setter, and setters exist only for the modifiable fields.
- `UserField`: an enum of `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
- `UserValidator`: static checks for DNI, email, password and card, without regex.
- `UserRepository`: the persistence class. It loads and saves the users file (`;`-separated lines) using `java.nio.file`.
- `UserService`: create, delete and update, covering uniqueness, credential checks and validation. It calls the repository to save after each successful mutation.
- `CommandProcessor`: parses one line (`split(";", -1)`), dispatches it, and prints the result to an injected `PrintStream`. It returns whether processing should continue, which is how EXIT is implemented.
- `CommandRunner`: reads lines from any `BufferedReader` (stdin or file), skips blank lines, and stops when the processor signals EXIT. Both modes use the same instance type, which satisfies the "identical processing" requirement.

Alternative considered: a Command-pattern class per command. It was rejected as unnecessary for five commands; the source says design patterns are not required in this delivery.

**Command keyword**: the first `;`-separated token, trimmed and compared case-sensitively (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`). Field values are taken as-is, without trimming, because names can contain spaces (`Garcia Lopez`).

**Messages**: success prints `USER CREATED`, `USER DELETED` or `USER UPDATED`, as in the source example. Failures print a line starting with `ERROR:` followed by a reason. The exact error wording is not specified in the source.

**Users file**: `users.txt` in the current working directory. `UserRepository` takes a `Path`, so tests use temporary files. A missing file means there are no users. Lines with the wrong field count are skipped with a warning on stderr instead of aborting start-up.

**JAR name**: `<finalName>amusement-park</finalName>` produces `target/amusement-park.jar`. The source uses the placeholder `target/nombre-aplicacion.jar` ("application-name").

**Manifest**: `maven-jar-plugin` with `mainClass=es.upm.amusementpark.App`. No shading is needed because there are no runtime dependencies.

**Testing**: JUnit Jupiter in test scope only. The quickstart archetype ships with JUnit, and the "no external libraries" rule applies to the application.

**Symbol definition**: any character that is not a letter, a digit or whitespace (`Character.isLetter`, `isDigit`, `isWhitespace`).

**Email**: `split("@", -1)` must yield exactly two non-empty parts. In the part after `@`, `lastIndexOf('.')` separates the domain from the extension: the domain must be non-empty, contain no empty dot-separated label, and be followed by an extension of exactly 3 characters. No regular-expression pattern is used.

## Risks / Trade-offs

- [Ambiguity: JAR name placeholder] → `amusement-park.jar` chosen and documented. Renaming later is a one-line `finalName` change.
- [Ambiguity: mandatory per-milestone commit messages] → The BOTL orchestrator makes one commit per proposal file, so the eleven `[F0-C01]`…`[F1-C09]` messages are listed in that commit's body. They are not separate commits.
- [Ambiguity: source example wraps `Clave1!` across two lines (`Clave1` / `!`)] → Treated as a document line-wrap artefact. Commands are single lines.
- [Ambiguity: empty NAME/SURNAME/PHONE values] → Not specified. Empty field values are rejected for create and update so that a persisted line always round-trips with 7 fields.
- [Values containing `;`] → They cannot be expressed in the command syntax, so they cannot reach the file.
- [Plain-text passwords in the file] → Required by Delivery 1 (no encryption). Delivery 3 introduces BCrypt.
