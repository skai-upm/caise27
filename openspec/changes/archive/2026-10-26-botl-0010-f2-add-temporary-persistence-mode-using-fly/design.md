# Design

## Context

`App.run(args, in, out, usersFile)` builds every storage from sibling file names of `usersFile`; storages take a `Path` and know nothing about modes. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One switch point (where paths are decided); zero changes to storages/managers.

**Non-Goals:**
- Copying permanent data into the temporary directory (a fly run starts empty); configurable base directories from the command line.

## Decisions

- **`DataDirectory`** (new): holds `path : Path` and `temporary : boolean`. Factories `permanent(Path)` and `temporary(Path base, LocalDateTime start)` (creates `base/yyyyMMdd_HHmmss`, appending `_1`, `_2`… on collision). `resolve(String fileName)` returns the file path; `close()` (AutoCloseable) deletes the directory tree only if temporary, idempotently. Alternative: a boolean threaded through every storage — rejected, it would spread mode logic into repositories.
- **`App.run(args, in, out, Path dataDir, Path tmpBase)`**: parses args (`--fly` anywhere; first other argument = command file), opens a `DataDirectory` in try-with-resources so cleanup happens on EXIT, EOF, file errors and exceptions. `main` passes `data` and `tmp`. The start timestamp uses the real clock: it names a directory and is not a business rule, so the system-date rule is unaffected.
- **Shutdown hook**, registered by `App.run` for the duration of the execution and removed afterwards, also closes the temporary directory if the JVM is terminated abruptly (e.g. Ctrl+C); `close()` being idempotent makes the double call harmless.
- **`.gitignore`**: add `tmp/`.

## Risks / Trade-offs

- [Kill -9 skips cleanup] → unavoidable; directory names are unique so later runs are unaffected.
