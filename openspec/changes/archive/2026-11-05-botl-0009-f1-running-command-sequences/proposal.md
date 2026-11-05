# Proposal

## Why

Milestone F1-C07 (Delivery 1, step 9) requires the application to run a sequence of commands stored in a text file whose path is given as the first argument at start-up (source §10.2), in addition to interactive standard input (§10.1). Command behavior must be identical for both sources, with no second implementation of the operations, and a nonexistent or unreadable file must be handled in a controlled way. Today the application always reads standard input and ignores arguments.

## What Changes

- When started with at least one argument, the application treats the first argument as the path of a command file. It processes its lines in order, ignores blank lines, and ends when the file ends. Without arguments, it keeps reading standard input (interactive mode, unchanged).
- Both sources feed the same line-processing loop and the same `CommandProcessor`, so responses are identical whatever the source.
- A nonexistent or unreadable command file produces an `ERROR` message naming the file. The application then ends normally, with no stack trace and no data changes.

In scope: the file argument, ordered processing, blank-line handling, controlled handling of a missing or unreadable file.

Out of scope: `HELP` and `EXIT` (next milestone "execution control"; they still answer as unknown commands), and multiple file arguments (only the first argument is used).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `command-interface`: adds requirements for command-file input, identical processing of both sources, and controlled handling of a missing or unreadable file. Existing interactive-mode, element-separation and error-handling requirements are unchanged.

## Impact

- `App.main` selects the input source; `App.run` is reused unchanged for both. New tests and end-to-end checks.
