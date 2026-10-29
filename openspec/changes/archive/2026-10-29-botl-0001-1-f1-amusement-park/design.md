# Design

## Context

The repository is empty apart from OpenSpec configuration. See proposal.md - Why. Constraints from the source: Java 21, Maven quickstart structure, Java standard library only, no regular expressions, no frameworks, file-based persistence, command parsing with basic String operations and `split`, and separation of user / main / persistence classes.

## Goals / Non-Goals

**Goals:**
- A small, layered code base that later deliveries (groups, tickets, attractions) can extend without restructuring.
- One command-processing path shared by interactive and file modes.
- Unit tests for every validation rule and operation, plus an end-to-end test of file mode and EXIT.

**Non-Goals:**
- Any Delivery 2/3 feature (system date/price, groups, tickets, attractions, encryption).
- Design patterns beyond plain class separation (not required in this delivery).

## Decisions

### Project coordinates and packaging
`groupId` `es.upm.etsisi`, `artifactId` `amusement-park`, base package `es.upm.etsisi.amusementpark`, layout of the apache-maven-quickstart archetype (1.5) with `maven.compiler.release=21`. `<finalName>amusement-park</finalName>` and a `maven-jar-plugin` manifest `mainClass` make `target/amusement-park.jar` executable. With no runtime dependencies a plain JAR suffices; shade/assembly plugins are unnecessary (alternative rejected: fat-JAR plugins add build complexity for no benefit).

### Classes
| Class | Responsibility |
|---|---|
| `App` | Entry point only: chooses interactive vs. file input and delegates to `CommandProcessor`. No parsing, no file I/O for users. |
| `CommandProcessor` | Splits a line with `split(";", -1)`, dispatches by the first element, validates argument count, returns the textual result, and tracks whether EXIT was requested. `run(BufferedReader, PrintStream)` is the single loop used by both modes (skips blank lines, stops after EXIT). |
| `User` | Encapsulated data holder; `dni` is `final` with no setter. |
| `UserField` | Enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`. |
| `UserValidator` | Static validation of DNI, email, password, card (character loops / `split` / `indexOf`; no regex). |
| `UserService` | Business operations create/delete/update, uniqueness checks, credential checks; persists via `UserRepository` after each successful change. Errors are signalled with `UserOperationException` carrying a message. |
| `UserRepository` | Loads/saves the users text file (`java.nio.file.Files`); one user per line, `;`-separated. Path injected (default `users.txt`) so tests use temporary directories. |

Alternative considered: putting validation inside `User` setters. Rejected because validation must also run before a user exists (creation) and against other users (uniqueness), which belongs to the service.

### Parsing without regex
`String.split(";", -1)` and `split("@", -1)` use single literal characters (no regex metacharacters) as the source explicitly allows `split`. The email extension is found with `lastIndexOf('.')`. Validation checks use `Character.isDigit/isUpperCase/isLowerCase/isLetter/isWhitespace`. DNI letter: `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`.

### Persistence strategy
Whole-file rewrite after each successful mutation (`Files.write` of all lines). Simple and correct for the expected data size. Lines that do not have 7 fields are skipped on load with a warning on stderr so a corrupt line cannot crash start-up.

### Output and errors
Successful operations print `USER CREATED`, `USER DELETED`, `USER UPDATED`; errors print `ERROR: <reason>`. All exceptions from a command are caught inside `CommandProcessor`, so a bad line never stops processing. A missing/unreadable command file prints `ERROR: ...` to stderr and the process exits with status 1.

### Credentials check
For DELETE/UPDATE the user is located by email; unknown email and wrong password produce distinct error messages ("user not found" / "incorrect password").

## Risks / Trade-offs

- [JAR name differs from placeholder `nombre-aplicacion.jar`] → Documented in proposal ambiguities; changing `finalName` is a one-line edit.
- [Plaintext passwords in `users.txt`] → Required by the source (no encryption in Delivery 1); the runtime file is git-ignored.
- [Single commit for all milestones] → Flagged in proposal; milestone messages recorded in the commit body.
- [Whole-file rewrite is not atomic] → Acceptable at this scale; write to a temp file then move to reduce corruption risk.
