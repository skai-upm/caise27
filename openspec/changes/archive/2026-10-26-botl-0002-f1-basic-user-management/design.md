# Design

## Context

The codebase is the quickstart skeleton: `es.upm.parque.App` prints "Hello World!" and `AppTest` is a placeholder. Constraints from the proposal: Java 21 standard library only, no regex, `String.split` for command parsing, no interfaces/patterns/layers beyond what the requirements need, and the user class, main class and persistence class must be separate.

## Goals / Non-Goals

**Goals:**
- Keep each responsibility in its own small class so it can be unit-tested without the terminal.
- Keep the main loop and the command handling decoupled from `System.in`/`System.out` so tests can drive them.

**Non-Goals:**
- HELP, EXIT and reading commands from a file (next delivery).
- Concurrency, file locking, or escaping `;` inside values (`;` is the command separator, so values can never contain it).

## Decisions

- **Classes (all in `es.upm.parque`)**:
  - `User` — encapsulated state (private fields, getters, setters for editable fields only; `dni` is `final`). No I/O.
  - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`, with a `parse(String)` helper returning `null` for unknown names.
  - `UserValidator` — static checks `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNotBlank`, using `charAt`, `Character` methods, `indexOf`/`lastIndexOf` and `split("@", -1)`.
  - `UserRepository` — reads/writes the users file (`Path` given in the constructor). `loadAll()` returns `List<User>`; `saveAll(List<User>)` rewrites the file. Lines with a wrong number of fields are skipped.
  - `UserManager` — business rules: uniqueness, credential checks, create/delete/update. Holds the in-memory list loaded from the repository and saves after each successful change. Failures are signalled with an `IllegalArgumentException`-style unchecked `UserException` carrying the user-facing message.
  - `CommandProcessor` — splits a line with `split(";", -1)`, trims, matches the operation, checks parameter count, calls `UserManager`, and returns the response string (`OK ...` / `ERROR ...`). Catches `UserException` and any other `RuntimeException` so no command terminates the program.
  - `App` — `main` wires repository → manager → processor with the default file `data/users.txt` and runs `run(BufferedReader, PrintStream)` over `System.in`.
  Alternative: put parsing in `App` — rejected because it makes command handling untestable without the terminal, and the next delivery needs the same processing for file input.
- **`split(";", -1)`** keeps trailing empty strings so `USER CREATE;...;` with an empty card is detected as a blank value rather than as a missing parameter count mismatch hidden by `split` dropping trailing empties.
- **Email split on `@` with limit -1** so `a@b@c.com` yields three parts and is rejected. The period checks use `lastIndexOf('.')` rather than `split("\\.")`, which would be a regular expression.
- **Email comparison case-insensitive** (`equalsIgnoreCase`) for lookup and uniqueness; stored as typed.
- **Persistence rollback**: if `saveAll` fails with an I/O error, `UserManager` reloads the list from the file so memory never diverges from disk, and the command reports `ERROR`.
- **Data file location** `data/users.txt` relative to the working directory, parent directories created on save; `data/` is git-ignored so runtime data is never committed.

## Risks / Trade-offs

- [Passwords are stored in plaintext] → Required by this epic (no encryption); a later delivery introduces hashing.
- [A corrupted line in the file would be silently dropped on the next save] → Acceptable for a single-user CLI; lines are only written by the application.
