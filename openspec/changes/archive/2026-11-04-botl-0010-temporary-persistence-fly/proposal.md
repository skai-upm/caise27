# Proposal

## Why

Every run writes to the permanent data files (`users.txt`, `groups.txt`, `tickets.txt`, `group-tickets.txt`, `attractions.txt`) in the working directory, so trying commands or running test scripts alters the real state. An isolated mode is needed that uses the same persistence code against throw-away files.

## What Changes

- New launch option `--fly`, usable in interactive mode (`java -jar target/parque.jar --fly`) and file mode (`java -jar target/parque.jar --fly input_fase2.txt`).
- At startup `--fly` creates a new directory `tmp/<yyyyMMdd_HHmmss>/` (e.g. `tmp/20260826_190315/`) and points every persistence file at it. The existing storage and manager classes are reused unchanged; only the configured paths differ.
- When the run terminates — by `EXIT`, end of input, a command-file error or JVM shutdown — that directory and all its files are deleted.
- Without `--fly`, behaviour and file locations are exactly as before.

## Capabilities

### New Capabilities
- `temporary-persistence-mode`: The `--fly` option, the per-run timestamped directory, path redirection and cleanup.

### Modified Capabilities
- `command-input-modes`: "Interactive mode" and "File mode" — the `--fly` option is not a command-file argument; the first other argument is.

## Impact

- `App` (argument parsing, data-file configuration, cleanup), new `TemporaryDataDirectory` helper, tests, `uml/model.puml` (new class).
- No change to repositories, managers, commands or file formats.
