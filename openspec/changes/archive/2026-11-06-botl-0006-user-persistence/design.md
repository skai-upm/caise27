# Design

## Context

See proposal.md – Why. Users are held by the in-memory `UserRepository`, created through `UserService` and `CommandProcessor`; `Main` wires them. The `user-management` spec forbids file logic in `User` and in the entry point.

## Goals / Non-Goals

**Goals:**
- File I/O isolated in one class; repository remains the single source of users for the service.
- File and memory never diverge after a successful command.

**Non-Goals:**
- Concurrency, file locking, encryption, or atomic-rename durability guarantees.

## Decisions

- **`UserFileStore` (package `es.upm.park.persistence`)**: `List<User> load()` and `void save(List<User>)` over a `Path`, using `java.nio.file.Files` with UTF-8. `save` rewrites the whole file (small data sets; simplest way to support delete/update later). Lines are built with `String.join(";", ...)` and parsed with `split(";", -1)`; lines without exactly 7 fields or blank lines are skipped on load. Fields cannot contain `;` because commands are themselves `;`-separated.
- **I/O failures** surface as `PersistenceException` (runtime) and `CommandProcessor` reports them as `Error: ...` messages, keeping the application running.
- **`UserRepository`**: optional store. With a store it loads all users in its constructor and, on `add`, saves the would-be list first and only then updates memory, so a failed save leaves both unchanged. The no-arg constructor stays in-memory for unit tests.
- **File location**: `Main` passes `Path.of(System.getProperty("park.users.file", "users.txt"))` — wiring only, no I/O logic in `Main`. Alternative: hard-coded path — rejected because tests would write into the project directory.

## Risks / Trade-offs

- [Source does not name the file] → `users.txt` in the working directory.
- [Source does not say what to do with corrupt lines] → skip them so start-up never fails; they are dropped on the next save.
