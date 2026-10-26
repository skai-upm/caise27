# Proposal

## Why

Commands can currently only be piped through standard input and there is no way to discover the available commands or end a session explicitly. Delivery 1 requires running command scripts from a file, a HELP command and a controlled EXIT, while keeping a single command-processing path for both input sources.

## What Changes

- Interactive mode (no arguments): read commands from standard input until `EXIT` or end of input.
- File mode (first argument = path): read the file line by line and execute each non-blank line in order, stopping at `EXIT`.
- Both modes feed the same command processor; no command logic is duplicated.
- New `HELP` command listing the available commands and their syntax.
- New `EXIT` command terminating the session in a controlled way; nothing after it is executed.
- Nonexistent/unopenable/unreadable files are reported and the application terminates without an unhandled exception.
- Blank lines are ignored.
- User-management behaviour is unchanged.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `command-interface`: terminal input now stops at `EXIT`; adds file input, execution-mode selection, shared processing, `HELP`, `EXIT`, invalid-file handling and blank-line handling.

## Impact

- `App` gains mode selection and a shared input loop; `CommandProcessor` gains `HELP` and `EXIT`.
- New/updated tests for both modes, HELP, EXIT and file errors.
- No new dependencies; user model untouched.
