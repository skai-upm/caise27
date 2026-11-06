# Proposal

## Why

Milestone F1-C07 requires a command sequence stored in a file to run when its path is given as the first argument. Commands run in file order, blank lines are ignored, and a nonexistent or unreadable file is handled in a controlled manner. Commands must behave the same as in interactive mode, through one integrated processing path. Because of the batch order, the shared processing loop and file argument already exist (introduced with execution control). This milestone makes the file-input contract explicit and verifies it against the complete command set, now that USER commands exist.

## What Changes

- Add an explicit requirement for the command-file argument (**ADDED** to `execution-control`): the first argument (or the second, after `--fly`) is a text file, one command per line, processed in order through the same processing path as standard input.
- Add tests:
  - A full Delivery 1 flow (`USER CREATE/UPDATE/DELETE`, `HELP`, `EXIT`) produces identical output from standard input and from a file.
  - File order is preserved.
  - A nonexistent path and a directory path (unreadable as a file) each print a controlled error and end normally.
- No production change is needed unless the tests reveal a defect.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: explicit command-file input requirement.

## Impact

- Tests (`CommandFileInputTest`) and spec only.
