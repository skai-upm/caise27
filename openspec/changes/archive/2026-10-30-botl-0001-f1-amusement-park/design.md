# Design

## Context

Greenfield repository (only OpenSpec scaffolding, README, LICENSE). Delivery 1 forbids runtime libraries, regular expressions, databases, frameworks and encryption. Later deliveries (F2, F3) will add layering, design patterns and new domain objects, so packages are organised by responsibility now to keep future changes local. See proposal.md for scope.

## Goals / Non-Goals

**Goals:**
- Executable JAR `target/nombre-aplicacion.jar` built by `mvn package` with JDK 21 release level.
- One command-processing path shared by interactive and file modes.
- Clear separation: model, validation, persistence, command processing, entry point.

**Non-Goals:**
- Design patterns (Singleton, DI, Builder) — not required in Delivery 1.
- Password hashing (encryption forbidden in Delivery 1).
- Validating telephone numbers, names or surnames (not specified).

## Decisions

1. **Coordinates and packaging.** `groupId es.upm.etsisi`, `artifactId amusement-park`, `<finalName>nombre-aplicacion</finalName>` so the contractual command `java -jar target/nombre-aplicacion.jar` works literally. `maven-jar-plugin` writes `Main-Class` into the manifest. `maven.compiler.release=21`. Alternative (shade/assembly plugin) rejected: no runtime dependencies to bundle.
2. **Package layout** under `es.upm.etsisi.amusementpark`:
   - `App` — entry point; picks interactive or file mode, wires objects. Contains no file I/O for users.
   - `model.User`, `model.UserField` (enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`). `User` has private fields, a `final` DNI with getter only.
   - `validation.UserValidator` — static checks for DNI, email, password, card; no regex.
   - `repository.UserRepository` — in-memory list plus load/save delegating to `repository.UserFileStore` (text file I/O). The user class and `App` never touch the file.
   - `controller.UserController` — create/delete/update business rules (validation, uniqueness, credentials); returns result messages.
   - `cli.CommandProcessor` — parses one line with `split(";", -1)`, dispatches, returns whether to continue; `cli.CommandRunner` — loops over a `BufferedReader` (stdin or file), skipping blank lines and stopping on EXIT. Both modes use the same runner, satisfying the "no duplicate implementation" requirement.
3. **Errors.** Business rule failures raise `IllegalArgumentException` with a message inside the controller; `CommandProcessor` catches it and prints `ERROR: <message>`. Any unexpected `RuntimeException` per line is also caught so a single bad command never terminates the app.
4. **Persistence.** File `users.txt` in the working directory (path configurable via constructor for tests). Full rewrite after every successful mutation (small data, simplest correct approach). Missing file = empty set; malformed lines skipped with a warning on stderr.
5. **Command matching.** Command keyword is the first `;`-element, matched exactly (uppercase), e.g. `USER CREATE`. Field names in `USER UPDATE` are matched against the enum by exact name. `DNI` is not an enum value, so it is rejected naturally.
6. **Email rule.** Split on `@` with limit -1, require exactly 2 parts and a non-empty local part; take the domain part, require it to contain a `.` and split at the last `.`: name part non-empty, extension length exactly 3.
7. **Symbol definition** for passwords: any character that is not a letter, digit or whitespace (`Character` methods).
8. **Tests.** JUnit Jupiter 5.11.4 in `test` scope only (the quickstart archetype itself ships a test dependency); it is not packaged in the JAR, so the runtime remains standard library only.

## Risks / Trade-offs

- [Source examples wrap `Clave1` / `!` across two lines — a document-formatting artefact] → Treated as the single value `Clave1!`, consistent with the persistence example and later commands.
- [`nombre-aplicacion` looks like a placeholder] → Used literally because path names are contractual; renaming later only touches `<finalName>`.
- [Users file location unspecified] → `users.txt` in the working directory; flagged here as an assumption.
- [Case sensitivity of commands, DNI letter and email uniqueness not specified] → Commands/fields exact uppercase; DNI letter must be uppercase; email uniqueness compares exact strings. Flagged as ambiguities, not new business rules.
- [Mandatory per-milestone commit messages] → The BOTL batch yields one commit per proposal file; messages are listed in the commit body. Flagged in proposal.md.
- [Passwords stored in plain text] → Required by Delivery 1 (encryption forbidden); Delivery 3 introduces BCrypt.
