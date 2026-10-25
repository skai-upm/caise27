# Design

## Context

`App.start(args, stdin, out, dataDir)` wires every repository from `dataDir.resolve(<file>)` and chooses interactive or file mode from `args`. `main` passes `Path.of("data")`. Repositories create parent directories when saving.

## Goals / Non-Goals

**Goals:**
- `--fly` changes only the directory passed to the existing wiring.
- Deterministic tests (injected base directory and timestamp).

**Non-Goals:**
- Configurable temporary base path or other options; changing repositories.

## Decisions

- **`App.launch(args, stdin, out, permanentDir, tmpBase, now)`** separates options from positional arguments (`--fly` anywhere in `args`; the remaining arguments keep their order), picks the data directory and calls `start`. `main` calls it with `Path.of("data")`, `Path.of("tmp")` and `LocalDateTime.now()` (the timestamp names a directory; it is not a business date, so it does not use `SystemDate`).
- **`TemporaryDirectory` helper** (`create(Path base, LocalDateTime now)` → `base/yyyyMMdd_HHmmss`, `_1`, `_2`… if it exists, created eagerly with `Files.createDirectories`; `deleteRecursively(Path)` walking the tree in reverse order).
- **Cleanup** in a `finally` block around `start`, so it runs after `EXIT`, end of input, file errors or runtime failures. `main` also registers a JVM shutdown hook that deletes the directory if it still exists, covering Ctrl+C. Deletion is idempotent.
- **Failure to create** the directory prints `ERROR CANNOT CREATE TEMPORARY DIRECTORY` and the application ends without touching `data/`.
- **`.gitignore`** adds `tmp/`.

## Risks / Trade-offs

- [Two executions starting in the same second] → Numeric suffix keeps directories independent.
- [A killed JVM (SIGKILL) cannot run cleanup] → Unavoidable; leftover directories are under `tmp/` and git-ignored.
