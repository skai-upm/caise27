# Design

## Context

The project is the archetype skeleton (`es.upm.parque.App`) built with Maven on Java 21 (see `openspec/specs/project-build`). Only the Java standard library may be used; no regular expressions, frameworks or patterns introduced for future needs.

## Goals / Non-Goals

**Goals:**
- Keep user state, terminal interaction, command interpretation and file storage in separate classes.
- Make validation and business rules unit-testable without the terminal or the real data file.

**Non-Goals:**
- Interfaces/patterns for future extensibility, HELP/EXIT/file input (next epic), encryption.

## Decisions

- **Classes (package `es.upm.parque`):**
  - `User` – encapsulated state; `dni` is `final` with no setter.
  - `UserField` – enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
  - `UserValidator` – static checks for DNI, email, password, card; no regex (`charAt`, `Character.isDigit`, `indexOf`, `split("@", -1)`).
  - `UserRepository` – reads/writes the `;`-separated file; receives/returns `List<User>`. Path is passed to the constructor (default `data/users.txt`), creating parent directories on save.
  - `UserManager` – in-memory list loaded from the repository; enforces validation, uniqueness and credential checks; saves through the repository after each successful mutation. Failures are signalled with `IllegalArgumentException` carrying a user-facing message.
  - `CommandProcessor` – splits a line with `String.split(";", -1)`, splits the operation part with `split(" ")`, checks the parameter count, delegates to `UserManager` and returns an `OK ...`/`ERROR: ...` string. It never touches the file.
  - `App` – `main` builds repository → manager → processor and loops over standard input lines, printing each response.
  Alternative: putting command handling in `App` — rejected so `App` stays limited to terminal interaction and the processor can be reused by the file mode planned next.
- **Whole-file rewrite on every change.** Simple and consistent for small data sets; alternative append/patch updates add complexity without benefit.
- **Email check**: split on `@` (exactly two parts, both non-empty); in the domain, the last `.` must exist with a non-empty part before it and exactly three characters after it.
- **Symbol** for passwords: any character that is not a letter, digit or whitespace.
- **Email uniqueness is case-insensitive**; DNI letters must be uppercase.
- **Values containing `;`** cannot occur because the command is split on `;`; empty values are rejected for all fields.
- **Malformed lines** in the data file (wrong field count) are skipped with a warning on standard error rather than aborting start-up.
- **`data/`** is added to `.gitignore` because it holds runtime state.

## Risks / Trade-offs

- [Plaintext passwords in the data file] → explicitly required by this epic; encryption is a later delivery.
- [Concurrent runs could overwrite each other's file] → single-user terminal application; accepted.
