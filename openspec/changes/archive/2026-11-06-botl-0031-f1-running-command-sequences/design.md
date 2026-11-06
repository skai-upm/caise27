# Design

## Decisions

- **No new mechanism**: `App.run` already opens the file argument with `Files.newBufferedReader` and passes it to the same `CommandProcessor.run` used for standard input. Opening a directory throws `IOException`, which is already reported in a controlled way. The change adds verification against the full command set rather than a second implementation.
- **Tests** use `App.run(args, stdin, out, baseDir)` with separate temporary base directories per run, so persisted data from one run can't influence the other when comparing outputs.
- **Defect found by the new tests**: on macOS, opening a directory succeeds and only the first read fails, which produced `Cannot read commands from <path>` instead of the specified message. `App` now checks `Files.isRegularFile` and `Files.isReadable` (and catches invalid paths) before opening, so every unreadable command file reports `ERROR: Cannot read command file: <path>`.
