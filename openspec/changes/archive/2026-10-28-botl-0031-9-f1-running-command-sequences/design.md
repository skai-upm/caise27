# Design

## Context

`App.run` chooses standard input or `Files.newBufferedReader(path)` and passes either to the same `CommandProcessor.run`, which skips blank lines and stops at `EXIT`; I/O failures opening or reading the file are reported as `ERROR: Cannot read command file: <path>`.

## Decisions

- No production change: the single processor already satisfies "no two implementations".
- **`CommandFileModeTest`** (integration, temporary working directories): runs the source examples via standard input and via a file and compares outputs and resulting state; checks blank lines, order and `EXIT`; runs with a directory path and with a missing path.
