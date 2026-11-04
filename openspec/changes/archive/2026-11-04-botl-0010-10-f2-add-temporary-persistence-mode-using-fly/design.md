# Design

## Context

`App.run(args, stdin, out, err, Path dataDir)` already builds every store from files inside `dataDir` (`Path.of("")` in production), and selects stdin or a command file from `args[0]`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- `--fly` changes only `dataDir`; cleanup on every termination path.

**Non-Goals:**
- Copying existing data into the fly directory, keeping fly data, configurable temp locations, other flags.

## Decisions

- **Argument handling**: `run` separates the literal `--fly` from the other arguments (any position); the first remaining argument, if any, is the command file. This keeps the documented `--fly input_fase2.txt` order and also accepts `input_fase2.txt --fly`.
- **Run directory**: `tmp/` + `LocalDateTime.now()` formatted `yyyyMMdd_HHmmss`, under a configurable root (`App.TMP_ROOT = Path.of("tmp")`, overridable in tests). If the directory exists (two runs in the same second), suffixes `_1`, `_2`, … are appended. Created with `Files.createDirectories` + `Files.createDirectory` (fails rather than reusing). The real clock is used here only to name the directory — it is not a business rule, so the system-date rule does not apply.
- **Cleanup**: the whole run (store construction and command processing) is wrapped in `try/finally` that deletes the run directory recursively (`Files.walk`, deepest first). `main` additionally registers a shutdown hook that performs the same idempotent deletion, covering termination by signal (Ctrl+C). The parent `tmp/` is left in place.
- **Messages**: a line `INFO: --fly mode, temporary data in <dir>` is printed to standard error, keeping standard output identical to a normal run of the same commands.
- **Errors**: failure to create the run directory → `ERROR: Cannot create temporary directory ...` on stderr and exit status 1.

## Risks / Trade-offs

- [Ambiguity: should the fly run start with a copy of the persistent data?] → the source asks for an "independent" directory for "isolated runs", so it starts empty.
- [Process killed with SIGKILL] → no hook can run; the directory may remain under `tmp/`.
