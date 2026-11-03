# Design

## Context

`App.createProcessor(Path dataDir)` already builds every repository from one data directory (`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`); `main` passes `Path.of(".")`. See proposal.md.

## Goals / Non-Goals

**Goals:** switch the data directory for a run; reliable cleanup; zero changes to repositories/services.

**Non-Goals:** copying permanent data into the temporary directory (the source asks for an independent location); other options.

## Decisions

- **Argument parsing:** `--fly` is recognised anywhere in `args`; the first remaining argument (if any) is the command file. With no remaining argument the run is interactive. Without `--fly` parsing is exactly as before (`args[0]` is the file).
- **Directory naming:** `tmp/` under the working directory plus `LocalDateTime.now()` formatted `yyyyMMdd_HHmmss` (e.g. `tmp/20260826_190315/`). If it already exists (two runs in the same second), a suffix `_2`, `_3`, … is appended so each execution has its own directory. Reading the real clock here is not a business rule, so the SYSTEM DATE rule is not affected.
- **Same repositories:** the temporary path is just the `dataDir` passed to `createProcessor`; nothing else changes.
- **Cleanup:** `main` wraps the run in `try/finally` and deletes the directory tree (files first, then the directory; `Files.walk` in reverse order). A JVM shutdown hook performs the same deletion if the process is interrupted (e.g. Ctrl+C); deletion is idempotent. The parent `tmp/` is removed too when it becomes empty. Cleanup failures are reported on stderr without a stack trace.
- `.gitignore` gets `tmp/`.

## Risks / Trade-offs

- [Abrupt kill (`kill -9`) leaves the directory] → unavoidable; harmless because each run uses a new directory.
- [Example jar name `target/parque.jar`] → the existing artifact name is kept; the option works with any jar name.
