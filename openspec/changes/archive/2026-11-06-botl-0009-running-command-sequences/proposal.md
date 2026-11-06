# Proposal

## Why

Milestone F1-C07 (Delivery 1, step 9) requires running a sequence of commands stored in a file whose path is the first argument of the JAR, with exactly the same observable behavior as interactive mode. Today the application only reads standard input and ignores its arguments.

## What Changes

- Without arguments, keep reading commands interactively from standard input (source 10.1).
- With a first argument, read commands from that text file instead, one per line, in file order, ignoring blank lines (source 10.2).
- Feed both sources through the same command processing, so a command's result is identical regardless of its source.
- Handle a nonexistent or unreadable command file in a controlled way: print an error message and end normally, without a stack trace.

### In scope
- Source sections 10.1 and 10.2 as far as input selection and processing are concerned.

### Out of scope
- `HELP` and `EXIT` behavior (F1-C08); until then these lines are reported as unknown commands, in both modes alike.

## Capabilities

### New Capabilities
<!-- None: input sources belong to the existing command-interface capability. -->

### Modified Capabilities
- `command-interface`: interactive input now applies when no argument is given; a new file input mode with controlled handling of unreadable files.

## Impact

- `Main` selects the input source and reuses its single processing loop; `MainTest` covers both modes. No new dependencies.
