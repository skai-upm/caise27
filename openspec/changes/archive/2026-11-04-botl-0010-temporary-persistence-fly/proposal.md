# Proposal

## Why

Running scenarios or tests through the application today writes into the real data files (`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`). A temporary mode is needed in which every persistence facility works normally but against a throw-away, per-execution directory, leaving the permanent data untouched.

## What Changes

- New launch option `--fly`, combinable with both interactive mode (`java -jar target/parque.jar --fly`) and file mode (`java -jar target/parque.jar --fly input_fase2.txt`).
- With `--fly`, at startup the application creates a new directory `tmp/<yyyyMMdd_HHmmss>/` (timestamp of the execution start) under the working directory, and all repositories use exclusively that directory: users, groups, tickets, group tickets and attractions. It starts empty.
- The difference is handled only by configuring the data directory given to the existing repositories; no repository or business logic is duplicated.
- When the application terminates — end of input, `EXIT`, a command-file error, or an interrupted JVM — the temporary directory and all its files are deleted.
- Without `--fly` the permanent-persistence behaviour is exactly as before.

## Capabilities

### New Capabilities
- `temporary-persistence-mode`: The `--fly` option, the per-execution timestamped directory, redirection of all persistence to it, and its deletion at termination.

### Modified Capabilities
- `command-input-modes`: "File mode with one argument" — the command file is the first argument other than `--fly`.

## Impact

- **Code**: `App` only (argument handling, data-directory selection, creation and recursive deletion of the temporary directory, shutdown hook). Repositories and services are unchanged.
- **Data**: permanent files untouched in `--fly` runs; transient `tmp/<timestamp>/`.
- **Assumptions recorded**:
  - `--fly` is recognised in any argument position (the source examples put it first); the remaining first argument, if any, is the command file.
  - If a directory for the same second already exists (two runs within one second), a numeric suffix `_1`, `_2`, … is appended so each execution still gets its own directory.
  - The `tmp/` parent is removed too when it is left empty, so a `--fly` run leaves nothing behind.
  - The `--fly` run starts from empty data (an "independent" directory); it does not copy the permanent data.
