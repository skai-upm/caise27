# Proposal

## Why

Milestone F1-C07 requires running a sequence of commands from a file given as the first argument, with exactly the same observable behaviour as interactive input and without two separate implementations of the operations: commands in file order, one per line, blank lines ignored, and nonexistent or unreadable files handled in a controlled manner. Because this batch applied F1-C09 (Delivery 1 integration) and F2-C08 (persistence and `--fly`) first, both input mechanisms already share one command processor; the Delivery 1 user commands needed to run the source examples were only completed by F1-C03…F1-C06.

## What Changes

- Verify with the source examples (now executable: `USER CREATE`, `USER UPDATE`, `HELP`, `EXIT`) that interactive mode and file mode produce identical output and state through the single command processor.
- Verify blank-line handling, file-order execution, stopping at `EXIT`, and controlled handling of a nonexistent file and of an unreadable path (a directory).
- Commit with the exact message `[F1-C07] Add command file input`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `application-execution`: command-file mode explicitly covers blank lines, unreadable files and identical processing with the Delivery 1 examples.

## Impact

- New integration tests; production code is expected to need no changes.
- **Flagged ambiguities:**
  - The source examples split passwords across two lines (`Clave1` / `!`, `Otra2` / `#`), an artefact of the source text; each command is used as a single line (`Clave1!`, `Otra2#`).
  - Ordering conflict (flagged since F1-C09): file-mode support was introduced earlier in this batch, so this milestone's commit mainly adds verification.
