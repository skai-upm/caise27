# Proposal

## Why

Running demonstrations or test command files currently modifies the permanent data in `data/`. A temporary execution mode is needed that uses all persistence facilities in an isolated, throw-away location, so runs and tests never alter the application's persistent state.

## What Changes

- New `--fly` parameter: at startup the application creates an independent temporary directory `tmp/<yyyyMMdd_HHmmss>/` (timestamp of that execution) and every repository (users, groups, tickets, group tickets, attractions) uses only files inside it.
- The difference between normal and temporary execution is only the configured persistence directory; repositories and managers are not duplicated or specialised.
- When the application terminates — end of input, `EXIT`, end of a command file, or interruption — the temporary directory and all its files are deleted.
- `--fly` works with interactive execution (`--fly`) and with a command file (`--fly input_fase2.txt`).
- Without `--fly`, the permanent `data/` behavior is exactly as before.

## Capabilities

### New Capabilities
- `temporary-execution`: The `--fly` mode: temporary persistence directory, isolation from permanent data, compatibility with both execution modes, and cleanup on termination.

### Modified Capabilities
- `command-processing`: "Execution mode selection" accounts for the `--fly` option before the optional command-file argument.

## Impact

- `App` (argument parsing, data-directory selection, cleanup) and a small helper for the temporary directory; `.gitignore` adds `tmp/`.
- New tests; no change to repositories or business classes.
