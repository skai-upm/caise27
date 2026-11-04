# Proposal

## Why

User commands can currently only be typed on standard input until end of input. Users need a repeatable batch mode (commands from a file), a way to discover the available commands (`HELP`) and a controlled way to stop (`EXIT`), all without duplicating the existing command logic.

## What Changes

- Without arguments, the application reads one command per line from standard input until `EXIT` or end of input.
- With an argument, the first argument is a command-file path; its lines are processed in order through the **same** command processing as interactive mode; blank lines are ignored.
- New `HELP` command lists the available commands and their syntax (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`).
- New `EXIT` command stops processing immediately in both modes; nothing after it runs.
- A missing, unopenable or unreadable command file is reported with an error message and the application stops with a nonzero status, never with an uncaught exception trace.
- User management, validation and persistence behavior is unchanged.

Source change ID suggestion `add-command-execution-modes-and-control` is replaced by the BOTL ID `botl-0003-3-f1-command-execution-and-control`.

## Capabilities

### New Capabilities
- `command-input-modes`: Selecting standard input or a command file from the launch arguments, reading lines in order, skipping blank lines, sharing one processing path, and controlled handling of file errors.
- `application-control`: General commands `HELP` and `EXIT`.

### Modified Capabilities
- `command-processing`: remove "Terminal command input" (stdin until end of input only), superseded by `command-input-modes`, which adds file mode and EXIT.

## Impact

- `CommandProcessor` gains `HELP` and `EXIT`; `Result` gains a terminate flag.
- New `CommandRunner` reads commands from any reader and stops on `EXIT`/end of input.
- `App` selects the input source and reports file errors; it still performs no user-file I/O.
- No changes to `User`, `UserValidator`, `UserManager`, `UserFileStore`; no new dependencies.
