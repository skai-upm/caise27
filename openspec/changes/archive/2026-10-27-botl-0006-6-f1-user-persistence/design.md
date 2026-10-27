# Design

## Context

`UserRepository` keeps users in memory; `CommandProcessor` adds users through it.

## Goals / Non-Goals

**Goals:** durable user storage with the exact line format; loading at start-up; testable with a temporary file.
**Non-Goals:** delete/update persistence (they reuse the same save when added), park data (Delivery 2).

## Decisions

- **`persistence/UserFileStore`** with `List<User> load()` and `void save(Collection<User>)`, using `java.nio.file.Files` (UTF-8). Serialization joins attributes with `;`; parsing uses `split(";", -1)` and requires exactly seven fields.
- **`UserRepository(UserFileStore)`** loads on construction and saves after every mutation (`add`, later `remove`/`update`). A no-argument constructor keeps a purely in-memory repository for unit tests.
- **Full rewrite on save**: the file is small; rewriting keeps it consistent with memory and avoids line-editing logic.
- **`Main.run(..., Path usersFile)`** overload: `main` passes `Path.of("users.txt")`; tests pass a temp file. `Main` only decides the location; it does not read/write.

## Risks / Trade-offs

- [Write failure (disk/permissions)] → the operation reports `ERROR: Could not save users` instead of crashing; in-memory state keeps the change.
- [Relative `users.txt` depends on the working directory] → documented; matches running `java -jar target/...` from the project root.
