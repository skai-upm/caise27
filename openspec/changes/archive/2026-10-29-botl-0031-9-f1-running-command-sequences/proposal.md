# Proposal

## Why

Milestone F1-C07 requires running a sequence of commands stored in a file given as the first argument, with the same observable behavior as interactive input and without two separate implementations: commands processed in order, blank lines ignored, and a nonexistent or unreadable file handled in a controlled manner. In this batch the single processor and the file argument already exist (added with HELP/EXIT and the Delivery 1 integration), but blank-line handling and unreadable files are not yet specified or verified, and the equivalence has not been verified with the user command sequences of the source.

## What Changes

- Specify and verify command-file processing: one command per line, in file order, blank lines ignored, identical results to interactive input for the source example sequence (USER CREATE ×2, USER UPDATE, EXIT), interactive mode keeps waiting for the next command after each one.
- Specify and verify controlled handling of an unreadable command file (for example a directory path), in addition to a nonexistent one.
- No change to the processing logic is expected.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `command-interface`: command-file processing rules (order, blank lines, unreadable file).

## Impact

- Tests only (plus any defect fix the tests reveal).

## Ambiguities (flagged)

- The source example wraps two passwords across lines (`Clave1` / `!`); they are read as single lines with `Clave1!` and `Otra2#`.
