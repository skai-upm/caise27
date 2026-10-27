# Proposal

## Why

Milestone F1-C07 requires running a sequence of commands stored in a file given as the start-up argument, with exactly the same observable behaviour as interactive input and without duplicating operation logic. Reading from a file already reaches the shared command processor (F0-C02/F1-C03), but the source's file-mode rules — order of execution, ignoring blank lines, controlled handling of unreadable files — are not yet specified or fully implemented (a blank line is currently reported as an unknown command).

## What Changes

- Ignore blank (empty or whitespace-only) lines in both input modes: they produce no output and no error.
- Specify that file commands run in the order in which they appear and through the same processor as interactive commands.
- Keep the controlled handling of a nonexistent/unreadable file (error message, non-zero exit, no stack trace).
- Commit with the exact message `[F1-C07] Add command file input`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `execution-modes`: file mode gains ordered processing and identical-behaviour guarantees; blank lines are ignored in both modes; interactive mode keeps waiting after each command.

## Impact

- `CommandProcessor` (blank-line handling); `MainTest`/`CommandProcessorTest` coverage of a mixed command file.
- Ambiguity flagged: the source example shows a password split across two lines (`Clave1` / `!;612345678;...`), which is a typesetting artefact; each command is treated as exactly one line, as the source states ("one command per line").
