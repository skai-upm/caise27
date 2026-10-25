# Design

## Context

The repository is empty apart from OpenSpec scaffolding and a Maven-oriented `.gitignore`. JDK 21+ and Maven are available locally. See proposal.md for motivation. The F1 statement imposes strong constraints: standard library only, no regular expressions, commands split with `String.split`, a minimum separation between the user model, the main class and the persistence class, and no speculative patterns or layers.

## Goals / Non-Goals

**Goals:**
- A quickstart-archetype project that builds with `mvn test` / `mvn package` on Java 21 and produces an executable JAR.
- A small, testable class set covering user management, validation, persistence and command processing.
- One command-processing path shared by interactive and file modes.

**Non-Goals:**
- Layered packages, interfaces, design patterns or dependency injection frameworks (explicitly postponed to later deliveries).
- Any domain beyond users (attractions, groups, tickets, passes), encryption, or a database.

## Decisions

### Project generation
Generate with `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DgroupId=es.upm.parque -DartifactId=parque -DinteractiveMode=false`, then move the generated content to the repository root. Set `maven.compiler.release` to 21 and configure `maven-jar-plugin` with `mainClass` `es.upm.parque.App` and `finalName` `parque`, producing `target/parque.jar`. The archetype's JUnit 5 test setup is kept.
*Alternative:* hand-write the `pom.xml` — rejected because the statement requires the archetype structure.

### Classes (single package `es.upm.parque`)
| Class | Responsibility |
|---|---|
| `App` | Entry point: chooses interactive or file mode from `args`, reads lines, delegates each to `CommandProcessor`, handles unreadable files. |
| `CommandProcessor` | Parses a line with `String.split(";", -1)`, dispatches `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`, prints `OK ...` / `ERROR ...`. Returns whether processing should continue. |
| `User` | Encapsulated user state; `dni` is `final` with no setter. No file access. |
| `UserField` | Enum of editable fields `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`. |
| `UserValidator` | Static validation helpers (DNI, email, password, card, non-empty), implemented with `String`/`Character` methods only. |
| `UserManager` | In-memory list of users: uniqueness checks, create/delete/update rules; asks `UserRepository` to save after each change. |
| `UserRepository` | Reads/writes the semicolon-separated users file; receives and returns `User` objects. |

`UserManager` is needed so that neither `App` nor `CommandProcessor` hold business rules, and so the rules are unit-testable without I/O on the console. It is a plain class, not a pattern.
*Alternative:* put the rules in `CommandProcessor` — rejected: mixes parsing with business rules and makes F2 additions harder to test.

### Result reporting
Business operations in `UserManager` throw `IllegalArgumentException` with a human-readable reason on rejection; `CommandProcessor` catches it and prints `ERROR <reason>`. Successful operations print `OK USER CREATED`, `OK USER DELETED`, `OK USER UPDATED`. `CommandProcessor` writes to an injected `PrintStream` so tests can capture output.

### Command parsing
`line.split(";", -1)` keeps trailing empty fields so that `USER CREATE;...;` with an empty card is detected as an empty value, not as a missing parameter. The first token is trimmed and uppercased to obtain the operation (`USER CREATE`); parameters are trimmed. Exact parameter counts are enforced per command (CREATE 7, DELETE 2, UPDATE 4, HELP/EXIT 0).

### Validation details
- DNI: length 9, first 8 `Character.isDigit`, last a letter; `Integer.parseInt(first8) % 23` indexes `TRWAGMYFPDXBNJZSQVHLCKE`; compared uppercase. Stored uppercase.
- Email: `split("@", -1)` must give exactly 2 non-empty parts; the domain part must contain `.`; the substring before the last `.` must be non-empty and the extension after it exactly 3 characters (and contain no further `.` by construction).
- Password: one pass over characters with `Character.isUpperCase/isLowerCase/isDigit`; a symbol is any non-letter, non-digit, non-whitespace character.
- Card: length 16 and all `Character.isDigit` (ASCII `0`–`9`).

### Persistence
Default file `data/users.txt` relative to the working directory (directory created on first save). `UserRepository` takes the path in its constructor so tests use temporary directories. `save` rewrites the whole file (small data volumes; simplest consistent approach). `load` returns an empty list when the file is missing and skips malformed lines with a warning on `System.err`.

### Execution modes
`App.main(args)`: with no arguments it wraps `System.in` in a `BufferedReader`; with one argument it opens the file with `Files.newBufferedReader`. Both readers are passed to the same `run(BufferedReader, CommandProcessor)` loop that skips blank lines and stops when `process` returns `false` (EXIT) or input ends. `IOException`/`InvalidPathException` opening or reading the file prints `ERROR ...` and returns normally.

## Risks / Trade-offs

- [Passwords stored in plaintext] → Accepted by F1 (encryption explicitly excluded); revisited in a later delivery.
- [Rewriting the full file on every change is O(n)] → Acceptable for laboratory data sizes.
- [Data file in working directory depends on launch location] → Documented default; tests always use temp paths.
- [`;` inside a value would corrupt the file] → Impossible via commands because `;` is the command separator.

## Migration Plan

Greenfield; no migration. Rollback is reverting the commit.
