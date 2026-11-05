# Proposal

## Why

Milestone F1-C07: the application can only be used interactively. The assignment requires it to also run a sequence of commands stored in a text file whose path is given as the first argument, with identical command behavior and a single implementation of the operations.

## What Changes

- `java -jar target/amusement-park.jar input.txt` processes the commands of `input.txt`, one per line, in file order, ignoring blank lines, then terminates.
- Both input sources feed the same command-processing path (same responses for the same command).
- A nonexistent or unreadable file is reported with an `ERROR: ...` message and the application terminates in a controlled way (no stack trace, exit status 1).
- Out of scope: HELP/EXIT (F1-C08).

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `executable-application`: adds the file mode launch option alongside the existing interactive mode (ADDED requirements; existing ones unchanged).

## Impact

- `App` selects the input source from `args`; `InteractiveSession` is reused unchanged for both sources.
