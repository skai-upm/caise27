# Design

## Context

`App.run(args, stdin, out, usersFile)` builds every repository from `usersFile` and its siblings (`usersFile.resolveSibling("groups.txt")`, etc.). `main` passes `Path.of("users.txt")`, so the permanent data lives in the working directory. Repositories already take their path as a constructor argument, so pointing them elsewhere needs no repository change.

## Goals / Non-Goals

**Goals:**
- `--fly` changes only *where* data is stored, by choosing a different users-file path whose siblings land in the temporary directory.
- Guaranteed cleanup on every normal termination path.

**Non-Goals:**
- Copying the permanent data into the temporary directory (the source asks for an independent location).
- Any change to repositories, managers or commands.

## Decisions

- **Argument parsing.** If `args[0]` equals `--fly`, fly mode is on and the remaining arguments are interpreted exactly as before (none = interactive, first = command file). `--fly` is recognised only in first position, as in the source examples. In any other position it would be read as a file name, which is the existing behaviour.
- **Directory.** `tmp/` is placed next to the permanent users file (`usersFile.toAbsolutePath().resolveSibling("tmp")`), which is the working directory in production and a temp dir in tests. The name is `LocalDateTime.now()` formatted `yyyyMMdd_HHmmss`. If that directory already exists (two runs in the same second), `_1`, `_2`, … are appended. It is created with `Files.createDirectories`, and a failure is reported as `ERROR` with status 1. The fly users file is `<dir>/users.txt`, and the sibling rule places all other files there.
- **Clock use.** Reading the real clock for the directory name is technical, not a business rule. The `system-date` requirement is modified to say this explicitly.
- **Cleanup.** `run` wraps the whole execution after directory creation in `try/finally` and deletes the directory tree (`Files.walk`, reverse order). While a fly run is active, `run` also registers a JVM shutdown hook (removed again after cleanup), so an interrupted process (Ctrl+C) cleans up too. Deletion is idempotent, so both mechanisms can run. A deletion failure is reported as `ERROR: Could not delete temporary directory ...`. `tmp/` itself is kept, because it may contain other runs' directories.
- **No extra output.** Fly mode prints nothing additional, so command output is identical to a normal run, which keeps scripted comparisons possible.
- **Testability.** `App.run` keeps its signature. A package-private `App.flyDirectory()` is not needed: tests look under `<tempdir>/tmp/` during and after the run.

## Risks / Trade-offs

- [Data in `--fly` is lost at the end] → intended. Users who want to keep data run without `--fly`.
- [Abrupt kill (SIGKILL) leaves the directory] → unavoidable; `tmp/` is git-ignored.

## Open Questions

- Whether `--fly` should start from a copy of the permanent data: the source says "independent temporary directory", so it starts empty.
