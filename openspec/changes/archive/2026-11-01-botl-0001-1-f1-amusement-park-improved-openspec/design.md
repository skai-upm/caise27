# Design

## Context

The repository contains only OpenSpec scaffolding; there is no Java code. See proposal.md for motivation and scope. Constraints from the source request: JDK 21, Maven Quickstart layout, Java standard library only, no regular expressions, commands parsed with `String.split`, and no patterns, frameworks or layers introduced for later deliveries.

## Goals / Non-Goals

**Goals:**
- A small, flat class structure that satisfies the mandated responsibility separation (user / main+terminal / file storage) and keeps command logic in one place shared by both input modes.
- Every validation and rejection path leaves in-memory and on-disk state unchanged.
- Business logic testable without a terminal or the real working-directory file.

**Non-Goals:**
- Interfaces, dependency-injection, command-pattern hierarchies or other abstractions not required by F1.
- Escaping of `;` inside field values (commands themselves are `;`-delimited, so such values cannot be supplied).
- Password hashing or any encryption (explicitly excluded in F1).

## Decisions

### D1. Project coordinates and packaging
Generate the project with `maven-archetype-quickstart` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, package `es.upm.amusementpark`) at the repository root, then set `maven.compiler.release` to 21. Configure `maven-jar-plugin` with a manifest `mainClass` of `es.upm.amusementpark.App` so `mvn package` yields an executable JAR.
*Alternative:* shade/assembly plugin — unnecessary because there are no runtime dependencies.

### D2. Classes and responsibilities
- `App` — entry point. Selects the mode from `args` (no args → `System.in`; first arg → file), opens the source as a `BufferedReader`, and hands it to the processor. Handles unreadable files by printing an error and returning. Contains no persistence or command logic.
- `CommandProcessor` — the single shared command path. `boolean process(String line)` parses with `String.split(";", -1)`, dispatches `USER CREATE` / `USER DELETE` / `USER UPDATE` / `HELP` / `EXIT`, prints a result or error message to a supplied `PrintStream`, and returns `false` only for `EXIT`. `void run(BufferedReader)` loops over lines, skips blank ones, and stops at `EXIT` or end-of-input. Both modes call `run`, so no command is implemented twice.
- `User` — encapsulated state; `dni` is `final` with no setter.
- `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed from the command's field token. DNI is intentionally absent, which makes it non-editable by construction.
- `UserValidator` — static checks `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, implemented with `charAt`, `Character.isDigit/isUpperCase/isLowerCase/isLetterOrDigit`, `indexOf`/`lastIndexOf` and `split`.
- `UserManager` — owns the in-memory user list, enforces validation and uniqueness, implements create/delete/update, and asks storage to save after each successful change. Returns a result message describing success or the reason for rejection.
- `UserStorage` — the only class touching the user file: `List<User> load()` and `void save(List<User>)`.

*Alternative:* putting dispatch in `App` — rejected because the brief forbids duplicating command logic between modes and keeping `App` thin keeps the main class free of business logic.

### D3. Validation-before-mutation
Create builds a candidate `User` only after all checks pass. Update validates the new value (and uniqueness for `EMAIL`) before calling the setter. Saving happens only after a successful mutation, so failures never touch the file. If saving throws `IOException`, the manager reports the error; the in-memory change is rolled back so memory and disk stay consistent.

### D4. Parsing rules
- Operation token = first `;`-separated token, trimmed (e.g. `USER CREATE`). Matching is exact (upper case), consistent with the documented syntax.
- Parameter counts are exact: CREATE 7, DELETE 2, UPDATE 4, HELP/EXIT 0. A wrong count yields an "invalid number of parameters" error rather than guessing.
- Parameters are trimmed of surrounding whitespace; `split(";", -1)` keeps trailing empty tokens so empty values are detected and rejected by validation.

### D5. Persistence format and location
One user per line, `nombre;apellidos;dni;email;password;telefono;tarjeta`, written with `Files.write` (UTF-8) and read with `Files.readAllLines`. Lines that are blank or do not have exactly seven fields are skipped on load. The application uses `users.txt` in the working directory; `UserStorage` takes the path in its constructor so tests use a temporary directory.
Passwords are stored in plain text because encryption is explicitly out of scope for F1.

### D6. Validation details
- DNI: length 9, first 8 chars digits, last char upper-case letter equal to `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`. Lower-case final letters are rejected (the source shows only upper case).
- Email: exactly one `@`; non-empty local part; the domain contains a `.`; the part before the last `.` is non-empty; the part after the last `.` has exactly 3 characters; no whitespace.
- Password: one upper-case letter, one lower-case letter, one digit, and one symbol — a character that is not a letter, digit or whitespace. No minimum length is imposed because the source does not state one.
- Card: length 16 and every character a digit.

### D7. Testing
JUnit (as provided by the archetype) tests for `UserValidator` (every valid/invalid example from the source), `UserManager` (create/delete/update success and rejection, uniqueness, persistence after restart using a temp file), `UserStorage` (round-trip), and `CommandProcessor`/`App` (parsing, HELP text, EXIT stopping a file, blank lines, unknown commands, missing file, interactive vs file equivalence via `StringReader` and captured output).

## Risks / Trade-offs

- [Plain-text passwords in `users.txt`] → Accepted for F1 per scope; isolated in `UserStorage` so a later delivery can change it.
- [A `;` inside a value would corrupt a line] → Cannot arise via commands since they are `;`-split; malformed lines are skipped on load.
- [Exact upper-case operation matching may surprise users typing lower case] → Matches the documented syntax; `HELP` shows the exact forms.
- [Archetype generation needs Maven Central access] → If unavailable, reproduce the archetype's standard output (pom, `App`, `AppTest`) by hand with the same structure.
