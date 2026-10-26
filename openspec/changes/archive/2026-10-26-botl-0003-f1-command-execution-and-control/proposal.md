# Proposal

## Why

Commands can currently only be typed into standard input and there is no way to see the available commands or to stop the application other than closing the input. Delivery 1 requires repeatable runs from command files, a HELP command, and a controlled EXIT, while keeping all user-management behavior intact.

## What Changes

- Interactive mode (no arguments): commands are read continuously from standard input until `EXIT` or end of input.
- File mode (first argument = path): the file is read line by line and each non-blank line is executed in order, using exactly the same command processing as interactive mode.
- New `HELP` command listing every available command with its syntax.
- New `EXIT` command that ends the application in a controlled way; in file mode no line after `EXIT` is executed.
- A non-existent or unreadable command file is reported with an `ERROR` message and the application terminates without a Java stack trace.
- Blank lines are ignored in both modes.
- No change to user commands or the user model.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `command-processing`: command input may come from standard input or a file chosen by the first argument; adds HELP and EXIT commands and controlled handling of unreadable command files.

## Impact

- `App` selects the command source from `args`; a new shared reader loop executes commands from any source.
- `CommandProcessor` gains `HELP` and `EXIT` handling.
- New/updated unit tests. No new dependencies.
