# Proposal

## Why

Today the application only reads commands from standard input until the stream ends: there is no way to replay a prepared list of commands from a file, no way to see which commands exist, and no explicit way to stop. Delivery 1 requires both execution modes, a HELP command and a controlled EXIT, while preserving all user-management behavior.

## What Changes

- **Execution mode selection**: with no arguments the application runs interactively on standard input; with one argument, the first argument is the path of a text file whose lines are executed as commands.
- **File mode**: the file is read line by line and each non-empty line is processed in order with exactly the same command processing as interactive mode.
- **HELP**: lists the available commands and their syntax (the three USER commands, HELP and EXIT). The list is derived from the commands the processor knows, so it grows with future commands.
- **EXIT**: terminates the application in a controlled way; in interactive mode no more input is read, in file mode no line after EXIT is executed.
- **Invalid input file**: a missing, unopenable or unreadable file is reported with an `ERROR` message and the application ends without an unhandled exception.
- **Blank lines** are ignored in both modes.
- No change to the user model or user commands.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `command-interface`: adds file input mode, execution-mode selection by program arguments, HELP and EXIT commands, invalid-file handling and blank-line skipping; terminal input now also stops at EXIT; the success-response rule is generalised to multi-line responses (HELP).

## Impact

- `App` (argument handling, file reading, stop on EXIT) and `CommandProcessor` (HELP, EXIT, shared command syntax list).
- Tests for both modes, HELP, EXIT and invalid files. No new dependencies; Java 21 and Maven unchanged.
