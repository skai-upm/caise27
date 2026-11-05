# Design

## Context

`UserFileStore.load()` skips only lines without 7 fields. `UserRepository(UserFileStore)` loads in its constructor; `App.main` builds it before choosing the input source. An `IOException` while reading is wrapped in `UncheckedIOException`, which currently escapes `main`. `CommandProcessor` already enforces validation and uniqueness for commands.

## Goals / Non-Goals

**Goals:**
- Loaded data meets the same invariants as created data.
- No uncaught exception at start-up.
- Automated evidence for the §16 flow and the §13 restrictions.

**Non-Goals:**
- Repairing or rewriting bad lines. They are ignored and reported, not fixed.

## Decisions

- **Validation on load lives in `UserRepository`**, not in `UserFileStore`. The store stays a pure format mapper. The repository already owns uniqueness, so it rejects users that are invalid (`UserValidator.isValid`) or whose DNI or email is already present. It also counts both these and the store's malformed lines. `UserFileStore.load` exposes its malformed-line count via `getMalformedLineCount()`.
- **Warning output from `App`**: after building the repository, `App` prints `WARNING: <n> invalid line(s) in users.txt were ignored` when the count is above 0. Printing stays in the entry point; the repository and store perform no console I/O.
- **Unreadable users file**: `App.main` catches the `UncheckedIOException` from repository construction, prints `ERROR: cannot read users file 'users.txt'` and returns before reading any command. Continuing with an empty store would overwrite the file on the next save.
- **Guard tests** use plain Java (`Files.walk` over `src/main/java`, plus parsing of `pom.xml` text with `indexOf`) so they need no extra libraries.

## Risks / Trade-offs

- [Ignored lines are dropped from the file at the next save] → The warning tells the operator before any change. This behavior is documented in the README.
