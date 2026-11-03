# Proposal

## Why

Trying out command sequences or running tests currently modifies the permanent data files. An isolated execution mode lets every persistence facility be used without touching the normal persistent state.

## What Changes

- New launch option `--fly`, usable with interactive mode (`java -jar ... --fly`) and file mode (`java -jar ... --fly input_fase2.txt`).
- With `--fly`, a new directory `tmp/<yyyyMMdd_HHmmss>/` (timestamp of the run) is created at startup and used as the location of **all** persistence files (users, groups, tickets, group tickets, attractions). The same repositories are used; only their paths change.
- The temporary directory and all its files are deleted when the application terminates, including via `EXIT`, end of input or a command-file error.
- Without `--fly`, behaviour and file locations are exactly as before.

## Capabilities

### New Capabilities
- `temporary-persistence-mode`: the `--fly` option, the per-run temporary data directory and its cleanup.

### Modified Capabilities
- `command-input-modes`: interactive/file mode selection now ignores the `--fly` option when locating the command file.

## Impact

- `App` only (argument parsing, data directory choice, cleanup). Repositories and services unchanged.
- New runtime directory `tmp/` (git-ignored).
