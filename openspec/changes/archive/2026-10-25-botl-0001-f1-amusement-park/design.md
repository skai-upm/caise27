# Design

## Context

The repository contains only OpenSpec scaffolding, a Maven-oriented `.gitignore`, `README.md` and `LICENSE`. There is no Java code yet. The local toolchain provides Maven 3.9 running on a JDK newer than 21, so the build must target Java 21 explicitly through `maven.compiler.release`. See proposal.md for motivation; behavior is specified in the delta specs.

## Goals / Non-Goals

**Goals:**
- A quickstart-derived Maven project at the repository root, producing `target/parque.jar` (executable).
- A small set of classes with clear responsibilities: user state, validation, user rules, file persistence, command interpretation and the terminal/file entry point.
- Fully unit-tested validation, user rules, persistence and command processing.

**Non-Goals:**
- Interfaces, frameworks or patterns introduced only for future deliveries.
- Encryption of passwords or telephone numbers.
- Concurrency or multi-process safety of the data file.

## Decisions

### D1. Project coordinates and layout
Generate with `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DgroupId=es.upm.parque -DartifactId=parque -Dpackage=es.upm.parque` and move the generated content to the repository root. Set `maven.compiler.release=21`, `<finalName>parque</finalName>`, and configure `maven-jar-plugin` with `Main-Class: es.upm.parque.App`. Keep the archetype's JUnit dependency and `AppTest`.
*Alternative:* keep the project in a `parque/` subfolder — rejected because later deliveries refer to `target/parque.jar` from the root.

### D2. Classes (package `es.upm.parque`)
- `User` — encapsulated state (private fields, getters, setters for editable fields only; `dni` is `final` with no setter). No I/O.
- `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD` used by `USER UPDATE`.
- `UserValidator` — static validation methods (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`) using only `String`/`Character` operations and `split`; no `java.util.regex`, no `String.matches`.
- `UserRepository` — reads/writes the users file (`load(): List<User>`, `saveAll(List<User>)`), converting each `User` to/from a `;`-separated line. The only class that touches the file.
- `UserManager` — keeps the in-memory user list, enforces uniqueness/credentials/update rules, and calls `UserRepository.saveAll` after each successful change. Methods return a result message (`OK ...` / `ERROR ...`).
- `CommandProcessor` — splits each line with `line.split(";", -1)`, dispatches `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`, checks parameter counts, and returns the output text plus whether processing must stop. Shared by both execution modes.
- `App` — `main` selects the input source (standard input or file from `args[0]`), wires `UserRepository` → `UserManager` → `CommandProcessor`, and loops over lines. It does not read or write the users file itself.

`UserManager` and `CommandProcessor` are not required by the minimum separation but keep `App` limited to terminal interaction and avoid duplicating command logic between modes. No interfaces are introduced.

### D3. Result messages
Successful operations print `OK <description>` (e.g. `OK USER CREATED`), failures print `ERROR <reason>`. This keeps output testable and matches the `OK ...` style used by later deliveries.

### D4. Persistence strategy
Rewrite the whole file on every change (users are few; simplicity over performance). The default path is `data/users.txt` relative to the working directory; parent directories are created on save. `UserRepository` takes the path in its constructor so tests use temporary files. Malformed lines (not exactly 7 fields) are skipped with a warning on standard error. Because fields come from `;`-split commands they can never contain `;`, so no escaping is needed. `data/` is added to `.gitignore`.

### D5. Validation details (assumptions recorded)
- DNI: exactly 8 digits plus an uppercase letter; letter = `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`. Lowercase letters are rejected.
- Email: exactly one `@`, non-empty local part, domain/extension split at the last `.` of the part after `@`; domain non-empty, extension exactly 3 letters; no whitespace.
- Password symbol: any character that is not a letter, digit or whitespace.
- Email uniqueness compares ignoring case; lookup by email for DELETE/UPDATE also ignores case.
- Every field must be non-empty after trimming; the telephone is otherwise stored as-is.
- Parameters are trimmed before use.

### D6. Execution modes
`App.run(String[] args, InputStream stdin, PrintStream out)` returns an exit code; `main` calls it with `System.in`/`System.out` and calls `System.exit` only for a non-zero code. With no arguments it reads `stdin`; with an argument it opens the file via `Files.newBufferedReader`. `IOException`/`InvalidPathException` produce `ERROR Cannot read command file: <path>` and exit code 1. Lines are trimmed; blank lines are skipped. Processing stops when `CommandProcessor` signals exit (EXIT) or input ends. Interactive mode prints a one-line banner suggesting `HELP`.

## Risks / Trade-offs

- [Passwords stored in plaintext in the data file] → Explicitly required for this epic (encryption is excluded); later deliveries can hash them.
- [Whole-file rewrite loses data if the process dies mid-write] → Write to a temporary file in the same directory and move it over the original.
- [Working-directory-relative data path] → Documented; tests always use temporary paths.
