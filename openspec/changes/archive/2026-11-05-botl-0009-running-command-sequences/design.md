# Design

## Context

`InteractiveSession` already takes any `BufferedReader` and a `CommandProcessor`; `CommandProcessor` ignores blank lines.

## Goals / Non-Goals

**Goals:** one processing path for both sources. **Non-Goals:** multiple files, globbing, EXIT semantics (F1-C08).

## Decisions

- **`App` chooses the reader**: no args → `System.in`; `args[0]` → `Files.newBufferedReader(path, UTF_8)`. The same `InteractiveSession`/`CommandProcessor` instances process either source, which guarantees identical behavior (no duplicated operation code).
- **Readability check before processing**: `Files.isRegularFile && Files.isReadable`; otherwise print `ERROR: cannot read command file '<path>'` to standard error and exit with status 1. Opening is also guarded for `IOException`. `App` only wires the source; reading lines stays in the session.
- **Exit status 1 for an unusable file** (assumption: the source only requires controlled handling); normal completion exits 0.

## Risks / Trade-offs

- [Extra arguments are ignored] → only the first argument is defined by the source.
