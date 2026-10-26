# Proposal

## Why

Every run of the application reads and writes the permanent data files, so trying out command sequences or running scripted tests alters the persistent state. A temporary execution mode is needed that exercises exactly the same persistence code against throw-away files.

## What Changes

- New program option `--fly`: at startup the application creates an independent directory `tmp/<yyyyMMdd_HHmmss>/` identifying the run by its timestamp, and every repository (users, groups, tickets, group tickets, attractions and any future persistent element) uses only files inside it.
- No repository logic is duplicated for this mode: the difference between normal and temporary execution is only the directory the repositories are configured with.
- When the run ends — end of input, `EXIT`, end of a command file, or the process being stopped — the temporary directory and all its files are deleted.
- `--fly` works with interactive execution (`java -jar target/parque.jar --fly`) and with a command file (`java -jar target/parque.jar --fly input_fase2.txt`).
- Without `--fly` the permanent persistence behaviour (files under `data/`) is unchanged.
- `tmp/` is git-ignored.

## Capabilities

### New Capabilities
- `temporary-persistence`: The `--fly` execution mode — creation of a per-run timestamped data directory, exclusive use of it by all repositories, and its deletion when the run ends.

### Modified Capabilities
- `command-interface`: execution mode selection now recognises the `--fly` option in addition to the optional command-file argument.

## Impact

- `App`: argument parsing, data-directory selection and wiring of all repositories from one directory; new `ExecutionOptions` and `DataDirectory` helpers.
- `.gitignore` gains `tmp/`; `uml/model.puml` updated for the new classes. Tests for both modes. No new dependencies.
