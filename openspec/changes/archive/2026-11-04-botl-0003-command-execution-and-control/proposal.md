# Proposal

## Why

User commands can currently only be typed on standard input, and the first program argument is used as an optional users-file override. Commands must also be runnable repeatably from a text file passed as the first argument, and users need a `HELP` command and a controlled `EXIT`, all through one shared command-processing path so terminal and file runs behave identically.

## What Changes

- **BREAKING (launch arguments):** the first `main` argument is now the path of a command file; the users file is always `users.txt` in the working directory (the previous optional override is removed, since it conflicts with the required meaning of the first argument).
- Without arguments: interactive mode reading standard input line by line until `EXIT` or end of input.
- With an argument: file mode reading the file line by line, in order, through the same command loop and processor as interactive mode.
- Blank lines are skipped in both modes (no output, no error).
- New `HELP` command listing the available commands and their syntax, from a single list that later deliveries extend.
- New `EXIT` command that stops processing immediately in both modes; nothing after it is executed.
- A missing, non-regular or unreadable command file (or a read failure) is reported with a single error line and the application exits with a non-zero status, without a stack trace.
- User create/delete/update behaviour, validation and persistence are unchanged.

## Capabilities

### New Capabilities
- `command-input-modes`: Selection of interactive vs. file input from the launch arguments, ordered line-by-line reading, blank-line skipping, shared processing and controlled file errors.
- `application-control`: General commands `HELP` and `EXIT`.

### Modified Capabilities
- `user-management`: "Command parsing" — blank lines are skipped by the input loop instead of producing an `ERROR`; "Terminal interaction" — standard input is read only in interactive mode and stops at `EXIT` or end of input.

## Impact

- `App` (argument handling, error reporting, exit status), `CommandProcessor` (HELP, EXIT, exit flag), new `CommandRunner` (shared input loop).
- Tests: `AppTest`, `CommandProcessorTest`, new `CommandRunnerTest`.
- No new dependencies; user model, validation and storage classes untouched.
