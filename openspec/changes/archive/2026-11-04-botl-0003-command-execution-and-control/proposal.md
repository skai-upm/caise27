# Proposal

## Why

User commands can currently only be typed on standard input, there is no way to see which commands exist, and the only way to stop is end-of-input. Running a repeatable command file and having `HELP`/`EXIT` makes the application usable both manually and in scripted, reproducible runs.

## What Changes

- Launch without arguments: interactive mode, reading commands from standard input until `EXIT` or end of input (already partly the case).
- Launch with one argument: the argument is a command-file path; its lines are processed in order, blank lines skipped, until `EXIT` or end of file.
- Both modes feed the **same** command processor; no command logic is duplicated.
- New `HELP` command listing every available command with its basic syntax, from a single list that later deliveries extend.
- New `EXIT` command that stops processing; nothing after it is executed.
- A missing, unopenable or unreadable command file is reported with an `ERROR` message and the application ends in a controlled way (no stack trace).
- User management, validation and persistence are unchanged.

## Capabilities

### New Capabilities
- `command-input-modes`: selection between standard input and a command file, line-by-line sequential reading, blank-line skipping, shared processing, and controlled handling of file errors.
- `application-control`: the general `HELP` and `EXIT` commands.

### Modified Capabilities
- `user-management`: the "Command parsing" requirement no longer says commands come only from the terminal, and its one-result-line rule is scoped to user commands (HELP prints several lines).

## Impact

- `App` gains mode selection; a new `CommandRunner` holds the single read-process loop; `CommandProcessor` gains `HELP` and `EXIT`.
- No change to `User`, `UserValidator`, `UserManager` or `UserFileRepository`.
- No new dependencies.
