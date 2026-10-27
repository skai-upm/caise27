# Proposal

## Why

Milestone F1-C07 requires running a sequence of commands stored in a file whose path is given at startup, with each command behaving exactly as in interactive mode through one shared processing path. In this repository the basic file input already exists, because milestone F1-C08 (HELP/EXIT) was applied first and needed it to make EXIT observable in file mode. This change completes and formalises the file-mode contract: one command per line in order, blank lines ignored, and controlled handling of a nonexistent or unreadable file.

## What Changes

- Specify and verify that file commands are processed in file order, one per line, with blank lines ignored, through the same processing as interactive mode (no second implementation of operations).
- A nonexistent command file and an unreadable command file are reported with distinct controlled error messages; the application ends normally without processing commands and without changing stored data.
- Interactive mode keeps waiting for the next command after each one.
- Record the mandatory milestone commit `[F1-C07] Add command file input` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: adds requirements for file-mode ordering and blank lines, and for controlled handling of nonexistent/unreadable command files.

## Impact

- `App` (file opening and error messages); tests in `AppTest`.
- No change to command semantics.
