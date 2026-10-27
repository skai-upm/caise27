# Design

## Context

`UserRepository` holds users in memory and is the single access point used by `UserService`. Constraints: standard library only; file I/O separate from `User` and `App`. F2-C08 will later add `--fly`, which must reuse the same repositories with a different directory, so the file location must be configurable.

## Goals / Non-Goals

**Goals:**
- Load on startup, save on every change, using the assignment's line format.

**Non-Goals:**
- Persisting other entities (Delivery 2), `--fly` mode (F2-C08), password hashing (Delivery 3).

## Decisions

- **`UserFileStorage(Path file)`** with `List<User> load()` and `void save(Collection<User>)`. Uses `Files.readAllLines`/`Files.write` (UTF-8), `String.split(";", -1)` for parsing. Separate class, so `User` and `App` stay free of I/O.
- **`UserRepository(UserFileStorage)`** loads at construction and calls `save` after `save(User)`/`delete`. The no-argument constructor keeps a purely in-memory repository (used by unit tests). Saving after each change (rather than only on EXIT) keeps the data when a file ends without `EXIT` or the process is interrupted.
- **File location (assumption, flagged):** the source does not name the file; `data/users.txt` relative to the working directory, parent directories created on first save. `App.run` takes the data directory as a parameter so tests (and later `--fly`) can redirect it.
- **Corrupt lines:** a line without exactly seven fields is skipped with a warning on the output instead of aborting startup (controlled handling; the source does not define this case).
- **Mandatory commit:** `[F1-C04] Add user persistence` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Whole file rewritten on each change] → Acceptable for the expected data sizes; keeps the format trivially consistent.
- [Running the JAR from different directories uses different data files] → Documented consequence of a relative path; consistent with `tmp/<timestamp>` paths used by F2-C08.
