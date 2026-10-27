# Proposal

## Why

Milestone F1-C08 adds the general `HELP` and `EXIT` commands: HELP lists the operations available and their syntax; EXIT ends the application in a controlled way, and in file mode no line after EXIT may be executed.

## What Changes

- Add `HELP`, printing at least the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`.
- Add `EXIT`, terminating the application normally (exit status 0); any subsequent command — from a file or from standard input — is not executed.
- Both behave identically in interactive and file modes (handled by the shared command processor).
- Commit with the exact message `[F1-C08] Add help and exit commands`.

## Capabilities

### New Capabilities
- `execution-control`: The general `HELP` and `EXIT` commands.

### Modified Capabilities
<!-- none -->

## Impact

- `CommandProcessor` (HELP text, exit flag); `Main` loop stops when exit is requested.
- Ambiguities flagged: EXIT prints no output (the source shows none); trailing elements after `HELP`/`EXIT` (e.g. `HELP;x`) are ignored; HELP additionally lists the modifiable field names for USER UPDATE as part of "basic syntax".
