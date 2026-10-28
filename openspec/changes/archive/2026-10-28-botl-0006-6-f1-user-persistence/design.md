# Design

## Context

`UserService` (validation + uniqueness) stores into the in-memory `UserRepository`; `App` wires repository → service → `CommandProcessor`. See proposal.md.

## Goals / Non-Goals

**Goals:** durable users with the mandated format, file logic isolated, tests never touching the real `users.txt`.
**Non-Goals:** park data persistence (Delivery 2), encryption.

## Decisions

- **`persistence.UserStorage` interface (`load()`, `save(List<User>)`) with `UserFileStorage(Path)`** implementation using `java.nio.file.Files`. The interface lets service tests use an in-memory fake.
- **Whole-file rewrite on each change** (small data set; keeps file consistent with memory, simple to reason about). Alternative: append-only — breaks once delete/update exist.
- **Save before committing to memory**: `UserService` computes the new user list, saves it, and only then updates the repository, so a failed save leaves memory and file consistent.
- **Startup**: `App(PrintStream out, PrintStream err, Path dataFile)` asks the storage to load and puts the users into the repository; `main` passes `users.txt`. Tests pass a temporary file.
- **Corrupt lines**: `UserFileStorage.load` collects warnings that `App` prints to stderr.

## Risks / Trade-offs

- [Relative file location depends on the working directory] → documented; same directory the user runs `java -jar` from.
