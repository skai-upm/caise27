# Proposal

## Why

Running test scripts against the application currently modifies the permanent data files in `data/`. A temporary execution mode is needed so complete runs (interactive or from a command file) can use every persistence feature in isolation without altering the permanent state.

## What Changes

- New `--fly` launch option: at startup the application creates a fresh directory `tmp/<yyyyMMdd_HHmmss>/` and every repository (users, groups, tickets, group tickets, attractions) uses only files inside it.
- The temporary directory and its files are deleted when the application ends, including after `EXIT`, end of input, or an interrupted run.
- `--fly` works with interactive mode (`--fly`) and with a command file (`--fly input_fase2.txt`).
- Without `--fly` the permanent persistence behavior is exactly as before.
- Repositories are not duplicated: the only difference between modes is the directory used to build repository paths.

## Capabilities

### New Capabilities
- `fly-mode`: temporary, self-deleting persistence directory selected with `--fly`.

### Modified Capabilities
- `command-processing`: the command file is the first argument that is not `--fly`; interactive mode applies when no command file is given.

## Impact

- `App` argument parsing and wiring (data directory chosen once and passed to all repositories); new `TemporaryDataDirectory` helper.
- `tmp/` added to `.gitignore`.
