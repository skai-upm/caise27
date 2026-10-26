# Design

## Context

`App.main` builds every repository from fixed constants (`data/users.txt`, …) and calls `start(args, in, out)`, which treats `args[0]` as the command file. Repositories already receive their file `Path` in the constructor, so they are path-agnostic.

## Goals / Non-Goals

**Goals:**
- One wiring routine that builds all managers from a single data directory; normal vs `--fly` only changes that directory.
- The whole run (option parsing, directory creation, processing, deletion) testable without `System.in`/`System.out` or the real clock.

**Non-Goals:**
- Copying permanent data into the temporary directory (the mode starts empty), keeping temporary directories after the run.

## Decisions

- **`ExecutionOptions`** record `(boolean fly, String inputFile)` with `parse(String[] args)`: `--fly` anywhere sets the flag; the first other argument is the input file (or `null`).
- **`DataDirectory`** helper: `PERMANENT = data`; `createTemporary(Path base, Clock clock)` creates `base/tmp/<yyyyMMdd_HHmmss>` (suffix `_1`, `_2`… if it exists) — the timestamp only names the directory and is not a business date, so reading the clock here does not break the system-date rule; `deleteRecursively(Path)` walks the tree bottom-up.
- **File names** become relative constants (`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`) resolved against the chosen directory by `App.createProcessor(Path dataDir, MainController)`.
- **`App.execute(String[] args, Path baseDir, Clock clock, InputStream in, PrintStream out)`** orchestrates a run: parse options, pick `baseDir/data` or a new temporary directory, build the processor, run `start(inputFile, in, out)` and, for `--fly`, delete the directory in a `finally` block. A JVM shutdown hook registered for the duration of the run also deletes it if the process is stopped (Ctrl+C); it is removed when the run ends normally. `main` calls `execute(args, Path.of(""), Clock.systemDefaultZone(), System.in, System.out)`.
- **`start`** now receives the optional input file name instead of the raw arguments.
- `.gitignore` gains `tmp/`. UML gains `ExecutionOptions` (`fly`, `inputFile`) and `DataDirectory` with usage from `App` (no incoming relationships).

## Risks / Trade-offs

- [Process killed with SIGKILL cannot run the hook] → Unavoidable; the directory is under `tmp/` and git-ignored.
