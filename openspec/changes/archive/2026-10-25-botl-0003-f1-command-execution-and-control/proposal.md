# Proposal

## Why

Delivery 1 user management only works by typing commands until standard input ends. The application must also run a prepared command file (for repeatable demonstrations and tests), describe its own commands, and terminate in a controlled way, while keeping all existing user-management behavior.

## What Changes

- Execution mode chosen by the arguments of `main`: no arguments → interactive mode on standard input; one argument → the first argument is the path of a command file read line by line.
- Both modes feed lines into the same command processing; no command is implemented twice.
- New `HELP` command listing the available commands with their syntax (at least the three `USER` commands, `HELP` and `EXIT`).
- New `EXIT` command terminating the application in a controlled way; in file mode, commands after `EXIT` are not executed.
- Blank lines are ignored in both modes.
- A command file that does not exist or cannot be opened or read is reported with an error message, and the application terminates without showing an unhandled exception.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: the terminal-input requirement becomes mode-independent (input ends with `EXIT` or end of input, HELP output spans several lines); new requirements for file mode, mode selection, `HELP`, `EXIT` and invalid command files.

## Impact

- `App` (mode selection, file reading, controlled termination) and `CommandProcessor` (`HELP`, `EXIT`, termination flag).
- User model, validation and persistence unchanged.
- New tests for both modes, HELP, EXIT and file errors.
