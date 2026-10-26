# Proposal

## Why

Users can only be managed by typing commands until standard input ends. Lab sessions and evaluations need to replay command scripts from a file, discover the available syntax, and stop the application explicitly — all without duplicating the user-management logic.

## What Changes

- Execution mode chosen by `main` arguments: no argument → interactive mode on standard input; first argument → path of a command file read line by line.
- Both modes feed the same command-processing mechanism; no command is implemented twice.
- New `HELP` command listing every available command with its basic syntax (Delivery 1: USER CREATE, USER DELETE, USER UPDATE, HELP, EXIT).
- New `EXIT` command that ends processing in a controlled way; in file mode no command after EXIT is executed.
- A command file that does not exist or cannot be opened/read is reported with a message and the application ends without an unhandled exception.
- Blank lines (terminal or file) are ignored.
- User-management behavior is unchanged.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-processing`: input can come from a file given as first argument as well as from the terminal; the command grammar admits single-word commands; new HELP and EXIT commands; controlled handling of invalid command files.

## Impact

- `CommandProcessor` gains HELP/EXIT and a shared read-execute loop; `App` selects the input source from its arguments.
- Tests for the new commands, the shared loop and file errors.
- No change to the user model, validation or persistence.
