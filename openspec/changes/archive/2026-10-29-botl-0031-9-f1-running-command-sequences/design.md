# Design

## Context

`App.run` reads standard input or `Files.newBufferedReader(args[0])` (after the optional `--fly`) and passes the reader to the single `CommandProcessor.run`, which trims lines and skips blank ones; I/O errors on the file are reported as `Error: could not read command file: <path>`.

## Goals / Non-Goals

**Goals:** specify and verify the F1-C07 rules. **Non-Goals:** new behavior.

## Decisions

- **Verification through `App.run`** with temporary base directories: the source sequence from a file vs. stdin (outputs compared), blank lines interleaved, a directory as command file, and interactive input ending without EXIT.
- **Same implementation**: the equivalence follows from both sources feeding the same `CommandProcessor.run`; the test guards it observably.

## Risks / Trade-offs

- [Persistence makes the second run see the first run's users] → each comparison uses its own temporary base directory.
