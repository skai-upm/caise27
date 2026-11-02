# Design

## Context

The repository contains only OpenSpec scaffolding, a README and a LICENSE; there is no Java code yet. See `proposal.md` for motivation and `specs/` for required behavior. Constraints: JDK 21, Maven, the `maven-archetype-quickstart` layout, Java standard library only at runtime, no regular expressions, commands parsed with `String.split`.

## Goals / Non-Goals

**Goals:**
- A small, readable class set with the three mandated responsibilities kept apart: user representation, application start/terminal interaction, and file persistence.
- One command-processing path reused by interactive and file modes.
- Failed operations never alter in-memory or persisted state.
- Behavior testable without launching a process (streams and file paths injected).

**Non-Goals:**
- Interfaces, design patterns, or layers beyond what Delivery 1 needs.
- Encryption or hashing of passwords/telephones, concurrency control, or file locking.

## Decisions

### D1. Project generation and coordinates
Generate the project at the repository root with `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart` using groupId `com.amusementpark`, artifactId `amusement-park`, package `com.amusementpark`, then set `maven.compiler.release` to `21`. The archetype's JUnit test dependency (test scope) is kept as the "basic testing structure". *Alternative:* hand-written POM — rejected because the source requires the archetype-generated structure.

### D2. Executable JAR
Configure `maven-jar-plugin` with `<mainClass>com.amusementpark.App</mainClass>` in the manifest. Because there are no runtime dependencies, a plain JAR is executable without shading. *Alternative:* `maven-shade-plugin`/assembly — unnecessary with zero runtime dependencies.

### D3. Classes and responsibilities
| Class | Responsibility |
|---|---|
| `App` | `main`: chooses interactive vs. file mode from `args`, opens the input, wires objects, reports file-open errors. No persistence code. |
| `User` | Encapsulated state; `dni` is `final` with no setter. No I/O. |
| `UserField` | `enum` of updatable fields: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. |
| `UserValidator` | Static checks for DNI, email, password, card and non-empty text, using `String`/`Character` operations only. |
| `UserFileStorage` | Reads/writes the semicolon-separated users file; takes and returns `User` objects. |
| `UserManager` | Holds the user list; create/delete/update with validation, uniqueness and persistence; returns a result message. |
| `CommandProcessor` | Splits a command line with `String.split(";", -1)`, dispatches `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`; reads lines from any `BufferedReader` and writes results to a `PrintStream`. |

`CommandProcessor.run(BufferedReader)` is the single loop used by both modes; `App` only decides which reader to pass (stdin or file). *Alternative:* separate loops per mode — rejected by the shared-processing requirement.

### D4. Command parsing
A line is trimmed; blank lines are ignored. It is split with `split(";", -1)` (limit −1 keeps trailing empty fields so missing values are detected). Field 0 is the operation, matched exactly (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`). Each operation requires an exact parameter count (7, 2, 4, 0, 0); otherwise an `ERROR:` message is printed. The `UserField` name in `USER UPDATE` is matched case-insensitively after trimming; `DNI` or any other value is rejected as a non-updatable field.

### D5. Validation details
- DNI: length 9, first 8 chars digits, last char a letter (compared upper-case); letter must equal `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`.
- Email: exactly one `@`; local part non-empty; domain part contains `.`; text before the last `.` non-empty; text after the last `.` exactly 3 characters. Emails are compared case-sensitively for uniqueness and lookup (the source does not request normalization).
- Password: scanned with `Character.isUpperCase/isLowerCase/isDigit`; a symbol is any char that is not a letter, digit or whitespace.
- Card: length 16 and every char `Character.isDigit` within `'0'..'9'`.
- Name, surname(s), telephone: non-empty after trimming; telephone stored as given.

### D6. Persistence strategy
`UserFileStorage` rewrites the whole file after every successful mutation (`Files.write` with UTF-8). Load at startup: missing file → empty list; lines with a field count other than 7 are skipped with a warning on stderr. `UserManager` applies a mutation to the in-memory list, saves, and if saving throws `IOException`, restores the previous state and reports an error so memory and file never diverge. The file path defaults to `users.txt` in the working directory; tests pass a temporary path.

### D7. Output conventions
Each processed command prints one line prefixed with `OK:` or `ERROR:` (HELP prints its multi-line listing). An invalid command file prints `ERROR: <reason>` to standard error and `main` returns normally — no stack trace.

## Risks / Trade-offs

- [Plain-text passwords in the users file] → Required by scope (encryption is explicitly out of Delivery 1); documented here.
- [Field values containing `;` cannot be represented] → Inherent to the command and file format; such values can't be entered because commands are split on `;`.
- [Concurrent runs sharing `users.txt` may lose updates] → Out of scope for a single-user terminal app.
- [Case-sensitive email uniqueness lets `Ana@correo.com` and `ana@correo.com` coexist] → Matches the literal source; can be revisited in a later delivery without changing the command format.
