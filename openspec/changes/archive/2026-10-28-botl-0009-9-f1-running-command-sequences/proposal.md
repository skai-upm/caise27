# Proposal

## Why

Milestone F1-C07 requires running a sequence of commands stored in a file whose path is given as the first argument, with exactly the same observable behavior as the same commands typed interactively, through a single implementation of the operations. The shared input loop introduced in F0-C02 already feeds both sources into one processor; this change makes the contract explicit, covers unreadable files and verifies equivalence end to end.

## What Changes

- Specify that file mode uses the first argument as the command file and processes one command per line in order (blank lines ignored), with output identical to interactive mode for the same commands.
- Specify controlled handling of nonexistent **and** unreadable files (e.g. a directory): error on standard error, non-zero exit, no stack trace.
- Specify that interactive mode prints each command's response before waiting for the next command.
- Add end-to-end tests comparing the output of the same command sequence from standard input and from a file.
- Commit with the mandatory message `[F1-C07] Add command file input`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-modes`: file mode takes the *first* argument and covers unreadable files; new requirement that command processing is identical for both sources; interactive mode answers each command before waiting for the next.

## Impact

- `App` (only if needed for unreadable-file handling and flushing), new tests in `AppTest`.
- `HELP` and `EXIT` appear in the statement's examples but belong to F1-C08; until then they are reported as unknown commands in both modes alike.
- Ambiguity flagged: arguments after the first are ignored (the statement only defines the first).
