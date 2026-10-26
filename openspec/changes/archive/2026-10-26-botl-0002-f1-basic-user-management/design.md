# Design

## Context

Baseline from `botl-0001`: single `es.upm.parque.App` with a hello-world main, JUnit 5 test. See proposal.md - Why. Constraints: Java 21 standard library only, no regex, `String.split` for command parsing, strict separation of User / main class / file storage.

## Goals / Non-Goals

**Goals:**
- Small set of cohesive classes meeting the mandated separation, unit-testable without a terminal.

**Non-Goals:**
- Interfaces, patterns or layers for future epics; encryption; listing/querying users (no command defined).

## Decisions

- **Classes (package `es.upm.parque`)**:
  - `User` – encapsulated state, `final` DNI, getters/setters only for editable fields; no I/O.
  - `UserField` – enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed from the command token with `valueOf` guarded by try/catch.
  - `UserValidator` – static checks (DNI, email, password, card, non-empty) using `charAt`, `Character.*`, `indexOf`, `split("\\.")`. Note: `String.split` takes a regex argument by API; using it with literal separators is mandated by the proposal and is not regex-based validation.
  - `UserStorage` – reads/writes the `;` file (`java.nio.file.Files`); `load()` returns `List<User>`, `save(Collection<User>)` rewrites the file. Skips malformed lines.
  - `UserManager` – in-memory registry (`LinkedHashMap` by DNI) with create/delete/update rules and uniqueness; calls `UserStorage.save` after each successful change. Throws `IllegalArgumentException` with a user-facing message on failure.
  - `CommandProcessor` – splits a line with `split(";", -1)` (keeps empty trailing fields so they are detected), splits the head with `split(" ")`, checks parameter count, dispatches to `UserManager`, and returns `OK: ...` / `ERROR: ...` text. Catches all `RuntimeException`s so no command can crash the loop.
  - `App` – builds storage (`data/users.txt`), manager and processor; loops over `System.in` lines printing responses.
  - Alternative considered: putting rules inside `CommandProcessor` — rejected to keep parsing separate from domain rules and testable on their own.
- **Whole-file rewrite on each change**: simplest correct approach for small data; avoids in-place line editing.
- **Email uniqueness case-insensitive**, DNI compared exactly (validation already enforces uppercase letter).
- **Fields containing `;`** cannot arrive through commands (they would be split), so the file format is safe without escaping.

## Risks / Trade-offs

- [Passwords stored in plain text] → mandated for this epic (encryption explicitly excluded).
- [Concurrent runs sharing the file] → out of scope; single-process CLI.
- [Data file in repo directory] → `data/` added to `.gitignore`.
