# Design

## Context

`App.main` builds every repository from the constant `DATA_DIR = data` and picks interactive or file mode from `args[0]`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One place that decides the data directory; repositories unchanged.
- Reliable cleanup on every way the JVM ends normally or by signal.

**Non-Goals:**
- Copying permanent data into the temporary directory; a `--fly` option in other positions.

## Decisions

- **Argument parsing**: `--fly` is recognised only as the first argument; the command file is the next argument (if any). Matches the documented invocations `--fly` and `--fly input_fase2.txt`.
- **`TemporaryDataDirectory`**: `create(Path base, LocalDateTime now)` makes `base/yyyyMMdd_HHmmss` (adding `_1`, `_2`… if it already exists, e.g. two runs in the same second) and `delete()` removes it recursively (idempotent). Timestamp is only a name, not a business date, so using the real clock is fine.
- **Cleanup**: `App` deletes the directory in a `finally` block after the command loop (covers `EXIT` and end of input), and also registers a JVM shutdown hook so Ctrl+C / SIGTERM delete it too. `delete()` being idempotent makes the double path safe.
- **Wiring**: extract `App.buildProcessor(Path dataDir)` that creates all repositories from the chosen directory, so normal and fly modes share the exact same construction code.
- `tmp/` is git-ignored.

## Risks / Trade-offs

- [Process killed with SIGKILL] → no hook runs; the stale `tmp/<timestamp>` directory remains but never affects later runs (each run uses a new directory).
