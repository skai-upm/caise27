# Design

## Context

`App.run(args, DataFiles, ...)` already receives every persistence path through the `DataFiles` record and builds all storages from it; `main` passes the five default file names. See proposal.md.

## Goals / Non-Goals

**Goals:**
- `--fly` only changes the `DataFiles` passed to the unchanged wiring.

**Non-Goals:**
- In-memory repositories, separate fly-specific repository classes, keeping fly data after the run.

## Decisions

- **Argument parsing:** `App.launch(args, workDir, startTime, in, out, err)` removes every `--fly` occurrence from the arguments. The remaining arguments go to the existing `run(...)`: none means interactive mode, otherwise the first one is the command file. Accepting `--fly` at any position is the smallest rule compatible with both source examples, which put it first.
- **`DataFiles.in(Path dir)`** factory resolves the five file names against a directory. Normal mode uses the working directory (identical paths to before); `--fly` uses the run directory.
- **`TemporaryDataDirectory`** (new helper):
  - `create(Path root, LocalDateTime start)` formats `yyyyMMdd_HHmmss`, appends `_n` while the name exists, and creates the directory with `Files.createDirectories`.
  - `deleteRecursively(Path)` walks the tree in reverse order.
  - The start time is read once in `main` (`LocalDateTime.now()`). This is not a business rule, so it does not conflict with the system-date rule.
- **Cleanup:** `launch` runs `run(...)` inside `try/finally`, deleting the directory in `finally`. `EXIT`, EOF and file errors all return through it, before `System.exit` is called. A shutdown hook deletes the directory on abnormal termination (Ctrl+C) and is removed after normal cleanup, so tests do not leak hooks. Only the run's own directory is deleted; the `tmp` parent is left in place.
- **Errors:** if the temporary directory cannot be created, print `ERROR: cannot create temporary directory ...` and exit with status 1.

## Risks / Trade-offs

- [`kill -9` cannot run cleanup] → Unavoidable; leftover directories have unique names and never affect permanent data.
- [A command file literally named `--fly`] → Cannot be used. Accepted, since the source reserves that token.
