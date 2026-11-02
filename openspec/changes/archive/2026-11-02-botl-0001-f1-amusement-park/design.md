# Design

## Context

The repository holds only OpenSpec scaffolding, a README and a Maven-oriented `.gitignore`; there is no Java code yet. JDK 21+ and Maven 3.9 are available locally. See proposal.md for motivation and specs/ for the behavioral contract. Delivery 1 forbids databases, frameworks, external runtime libraries, regular expressions and encryption, and requires separate user, main and persistence classes.

## Goals / Non-Goals

**Goals:**
- A small, layered command-line application whose command handling is independent of the input source, so interactive and file mode cannot diverge.
- Rejection paths that never leave partial state, in memory or on disk.
- Every spec scenario covered by an automated JUnit test, plus a packaged-JAR smoke run.

**Non-Goals:**
- Delivery 2/3 concepts (tickets, groups, attractions, dates, prices).
- Password hashing or encryption (explicitly forbidden in Delivery 1).
- Concurrency or multi-process access to the storage file.

## Decisions

### Project generation and coordinates
Generate the project with `mvn archetype:generate -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5` and move it to the repository root (the repo itself is the Maven project). Coordinates: groupId `es.upm.etsisi`, artifactId `amusement-park`, base package `es.upm.etsisi.amusementpark`. Set `maven.compiler.release` to 21. Alternative (hand-written pom) rejected: the source mandates the archetype.

### JAR name
The source writes `target/nombre-aplicacion.jar` in every command example. Set `<finalName>nombre-aplicacion</finalName>` so the documented commands work literally. Entry point configured via `maven-jar-plugin` manifest `mainClass`. No shading is needed because there are no runtime dependencies.

### Components
- `App` — entry point: chooses interactive vs file mode from `args` and delegates; contains no file I/O.
- `user.User` — encapsulated attributes, `final` DNI with no setter.
- `user.UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
- `user.UserValidator` — static, regex-free checks for DNI, email, password, card.
- `user.UserManager` — in-memory users, uniqueness checks, create/delete/update, delegates saving to the storage.
- `persistence.UserStorage` — loads/saves users in the `;` line format (`java.nio.file` only).
- `command.CommandProcessor` — parses one line with `split(";", -1)` and executes it, returning a result (message + whether to exit). Shared by both modes.
- `command.CommandRunner` — reads lines from any `BufferedReader` (stdin or file), skips blank lines, feeds `CommandProcessor`, stops on `EXIT`; also opens the command file and reports a controlled error when it is missing/unreadable.

Output is written to an injected `PrintStream` so tests can capture it. Alternative (static `System.out` everywhere) rejected for testability.

### Atomic mutations
`UserManager` builds the candidate new state, asks `UserStorage` to save it, and only then commits it in memory. Update operates on a copy of the user. If saving throws `IOException`, memory is untouched and an error is reported. This satisfies "rejected operations do not corrupt state" for both validation and I/O failures.

### Parsing details
- Elements are separated by `;`; each element is trimmed. The first element is the command keyword (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), matched case-sensitively as written in the source.
- Exact element counts are required (8 for create, 3 for delete, 5 for update, 1 for HELP/EXIT); otherwise a usage error is printed.
- Field names in `USER UPDATE` are matched against the enum names exactly (case-sensitive).
- Messages: success `USER CREATED` / `USER DELETED` / `USER UPDATED`; failures `ERROR: <reason>`. The source allows output to vary if behavior is equivalent.

### Validation rules where the source is terse
- DNI: length 9, first 8 chars digits, last char letter; check letter = `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23)`, compared case-sensitively (uppercase).
- Email: `split("@", -1)` must yield exactly 2 non-empty parts; the part after `@` is split at its last `.`; the domain before it must be non-empty and the extension must be exactly 3 characters. No restriction on which characters form the extension beyond not containing `.` (the source does not say letters-only).
- Password: symbol = any character that is not a letter, not a digit and not whitespace.
- Card: exactly 16 characters, each an ASCII digit `'0'..'9'`.
- DNI digits are likewise ASCII `'0'..'9'`.
- First name, surnames and telephone are not validated (the source only requires storing the telephone).

### Storage file
Default path `users.txt` in the working directory (the source names no file). On load, lines that do not have exactly 7 fields are skipped with a warning; a missing file means no users.

### Testing
JUnit (Jupiter) at test scope, as provided by the Quickstart archetype. It is not part of the runtime JAR, so the "standard library only" restriction for the delivered application holds. Tests use temporary directories for storage files.

### Git milestone history
The source prescribes 11 commits with exact messages (`[F0-C01] Initialize Maven project with JDK 21` … `[F1-C09] Complete delivery 1 integration`). This change runs inside an automated batch harness that owns Git history and creates exactly one commit per proposal after archive; making milestone commits during apply would also distort the per-phase working-tree measurements. The milestone messages are therefore **not** produced by this change. This is a known, explicitly flagged deviation from the source's process requirement, not a behavioral requirement of the application; the tasks follow the milestone order so the increments remain traceable.

## Risks / Trade-offs

- [Example lines in the source wrap the password across two lines (`Clave1` / `!`)] → Treated as a formatting artifact of the source document; the command is one line with password `Clave1!`.
- [Two HELP syntax variants appear in the source (`<firstName>` placeholders vs `FirstName`)] → Use the section 11 placeholder form, which is the stated minimum content.
- [Storage file holds plaintext passwords] → Required by the source format; encryption is forbidden in Delivery 1.
- [Malformed storage lines are dropped on the next save] → Warn on load; acceptable because the file is only written by this application.
- [`nombre-aplicacion` looks like a placeholder] → Literal name keeps every documented command runnable; renaming later only changes `finalName`.

## Open Questions

- Whether command keywords and update field names should be case-insensitive. Current choice: case-sensitive, as written in the source; can be relaxed later without changing other behavior.
