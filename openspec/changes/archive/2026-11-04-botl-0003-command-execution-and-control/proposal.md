# Proposal

## Why

Today `App` only reads commands from standard input and runs until end-of-input; there is no way to replay a prepared command file, to list available commands, or to stop explicitly. This change adds file-based execution sharing the exact same command processing as the terminal, plus `HELP` and `EXIT`, while keeping all user-management behaviour unchanged.

## What Changes

- Startup mode selection: no arguments → read commands from standard input; first argument present → treat it as the path of a command file read line by line in order.
- Both modes feed lines into one shared loop and the existing single `CommandProcessor`; blank lines are skipped in both.
- New `HELP` command listing every available command with its syntax (built from one list so later deliveries only add an entry).
- New `EXIT` command: acknowledges and stops processing; no later line (from terminal or file) is executed.
- A missing, unopenable or unreadable command file is reported with an `ERROR:` message on standard error and the application ends with exit status 1, never with a stack trace.
- `HELP`'s response spans several lines, so the "one line per command" response rule of `user-management` is relaxed to "the response's first line starts with `OK:`/`ERROR:`".

## Capabilities

### New Capabilities
- `command-input-modes`: Selection between standard input and command file, line-by-line ordered reading, blank-line skipping, shared processing, and controlled handling of unreadable files.
- `application-control`: The general `HELP` and `EXIT` commands.

### Modified Capabilities
- `user-management`: requirement "Command format and responses" — responses may span several lines (needed by `HELP`); the first line still starts with `OK:` or `ERROR:`. User commands themselves still answer with one line.

## Impact

- **Code**: `App` (mode selection, shared loop, file error handling, exit status), `CommandProcessor` (`HELP`, `EXIT`, exit flag). User model, validation, service and repository untouched.
- **Behaviour**: unknown-command handling unchanged; `HELP`/`EXIT` are no longer unknown commands.
- **Dependencies**: none.
- **Assumptions recorded**: `EXIT` prints `OK: Exiting`; file errors go to standard error and produce exit status 1 (normal termination, including via `EXIT` or end of input, is status 0); arguments after the first are ignored; `HELP`/`EXIT` followed by `;parameters` are rejected as wrong parameter count; a read error in the middle of a file stops processing after reporting it (commands already executed keep their effects).
