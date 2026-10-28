# Design

## Context

`App.execute` selects standard input or `Files.newBufferedReader(args[0])` and passes the reader to the same `run` loop → `processLine` → `CommandProcessor`. A directory path currently fails at read time with a generic `Error: <message>`. See proposal.md.

## Goals / Non-Goals

**Goals:** explicit, tested equivalence; controlled unreadable-path handling.
**Non-Goals:** new commands; changing response texts.

## Decisions

- **Keep the single loop**; no second implementation. Equivalence is verified by a test that runs the same lines through `execute` with stdin and with a file on fresh data files and compares output and stored users.
- **Check `Files.isRegularFile` and `Files.isReadable` before opening**, reporting `Error: cannot read file '<path>'` for both missing and unreadable paths (single message, exit status 1).
- **Flushing**: `System.out` flushes on `println`; `App` writes each response with `println`, so responses appear before the next read. Test uses an auto-flush stream.

## Risks / Trade-offs

- [Permissions-based unreadable test is platform dependent] → test uses a directory path, which is unreadable as a file everywhere.
