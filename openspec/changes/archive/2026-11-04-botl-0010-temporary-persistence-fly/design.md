# Design

## Context

`App.run(args, stdin, out, err, dataDir)` already builds every repository from one data directory (`dataDir.resolve("users.txt")`, …); `main` passes the working directory. So redirecting persistence only requires choosing a different `dataDir`.

## Goals / Non-Goals

**Goals:** isolate a run by configuration only; always clean up.

**Non-Goals:** copying permanent data into the temporary run; a flag to keep the temporary directory; changes to repositories or services.

## Decisions

- **Argument handling**: `run` splits `args` into `fly = contains("--fly")` and the remaining list; the command file is the first remaining argument. Interactive/file selection logic is otherwise unchanged.
- **Directory**: `createFlyDirectory(Path base, LocalDateTime start)` → `base/tmp/yyyyMMdd_HHmmss`, adding `_1`, `_2`… if it already exists; created with `Files.createDirectories` for `tmp` and `Files.createDirectory` for the run directory (atomic "fail if exists" so concurrent runs cannot share it). The timestamp is technical metadata, not a business date, so using the real clock does not conflict with `SYSTEM DATE`.
- **Wiring**: the chosen directory is passed as `dataDir` to the existing private wiring code (extracted into `runWithDataDir`), so the same repositories and services are used.
- **Cleanup**: `run` wraps the execution in `try/finally` and deletes the directory recursively (`Files.walk` in reverse order), then removes `tmp/` if empty. `main` additionally registers a JVM shutdown hook doing the same deletion (idempotent), covering Ctrl+C / SIGTERM. Deletion failures are reported on standard error without changing the exit status.

## Risks / Trade-offs

- [Kill -9 cannot be intercepted] → the directory may remain; it lives under `tmp/` and never affects permanent data.
- [Clock-based names] → collisions resolved with suffixes.
