# Proposal

## Why

User commands can only be typed until end of input, with no way to list commands, stop cleanly, or replay a prepared session. Supporting a command file as the first argument, plus `HELP` and `EXIT`, makes the application usable both manually and in repeatable batch runs, with one shared command-processing path.

## What Changes

- No arguments: read commands from standard input, one per line, until `EXIT` or end of input.
- First argument present: treat it as a command-file path, read it line by line in order, skip blank lines and run each line through the **same** `CommandProcessor` used interactively.
- New `HELP` command listing every available command with its syntax (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`).
- New `EXIT` command: stops processing immediately in both modes; later lines are never executed.
- Missing, non-openable or unreadable command files are reported with an error message and the application ends without an uncaught exception.
- User management, validation and persistence are unchanged.

## Capabilities

### New Capabilities
- `command-input-modes`: choosing standard input vs. command file from the launch arguments, sequential line reading, blank-line skipping, shared processing, and controlled handling of invalid files.
- `application-control`: the general `HELP` and `EXIT` commands.

### Modified Capabilities
- `user-management`: "Text command interpretation" now reads lines from the active input source (terminal or command file) instead of only the terminal.

## Impact

- `App` (mode selection and the single read/process loop) and `CommandProcessor` (HELP, EXIT, exit flag). No change to `User`, `UserValidator`, `UserFileStorage`, `UserService`.
- No new dependencies.
