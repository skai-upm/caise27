# Design

## Context

`UserRepository` (in-memory, DNI-keyed, insertion order) is used by `CommandProcessor`; `App` builds both and runs the stdin loop. Constraints: file I/O in its own class (§3.2, §7); no DB/frameworks/libraries.

## Goals / Non-Goals

**Goals:**
- Durable users with no change to command semantics.

**Non-Goals:**
- Atomic multi-process safety, backups, file location configuration.

## Decisions

- **`persistence.UserFileStore(Path)`** with `List<User> load()` and `void save(List<User>)` using `java.nio.file.Files` (UTF-8). `load` returns an empty list when the file is missing. Lines are split with `split(";", -1)` and must have exactly 7 fields; other lines are skipped (a malformed line must not crash start-up).
- **`UserRepository(UserFileStore)`** loads in its constructor and calls `save(findAll())` after `add`. The no-arg constructor stays as a pure in-memory store for unit tests. The repository remains the single place that "stores and retrieves users"; `CommandProcessor` is unchanged.
- **Save after every successful change** instead of on exit. EXIT does not exist yet and input may simply end, so this guarantees persistence. Rewriting the whole file is fine for Delivery 1 data volumes.
- **File `users.txt` in the working directory.** The source gives no name; this is a simple, documented default. It is git-ignored so local runs do not dirty the repository.
- **Write failures** (`IOException`) become an `UncheckedIOException` caught by `CommandProcessor`'s generic handler, which reports an `ERROR` and does not terminate the application.

## Risks / Trade-offs

- [A field containing `;` would corrupt the line] → Fields come from `;`-split commands, so they cannot contain `;`.
- [Whole-file rewrite is O(n) per change] → Acceptable for the assignment's scale.
- [A failed write after an in-memory add leaves memory and file inconsistent] → Reported as `ERROR`; the next successful save rewrites the full set.
