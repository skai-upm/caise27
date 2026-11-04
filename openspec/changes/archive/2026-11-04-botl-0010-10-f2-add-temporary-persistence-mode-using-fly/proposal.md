# Proposal

## Why

Every run reads and writes the persistent data files in the working directory, so trying out commands or running test scripts alters the real state. A temporary mode is needed in which all persistence works normally but on a per-run directory that is discarded at the end.

## What Changes

- New launch parameter `--fly`, usable with interactive and file execution: `java -jar target/parque.jar --fly` and `java -jar target/parque.jar --fly input_fase2.txt`.
- With `--fly`, the application creates at startup a run-specific directory `tmp/<yyyyMMdd_HHmmss>/` (timestamp of the run) and every persistent store (users, groups, tickets, group tickets, attractions) uses only files inside it; the directory starts empty.
- On termination — `EXIT`, end of input, a command-file error, or process shutdown — the directory and all its files are deleted.
- No repository is duplicated: the mode only changes the configured data directory.
- Without `--fly`, behavior is exactly as before.

The source requests the change name `add-fly-mode`; this batch uses the BOTL ID `botl-0010-10-f2-add-temporary-persistence-mode-using-fly`.

## Capabilities

### New Capabilities
- `fly-mode`: Temporary, per-run persistence location selected by `--fly`, and its cleanup.

### Modified Capabilities
- `command-input-modes`: interactive and file mode are selected from the arguments other than `--fly`.

## Impact

- `App`: argument parsing for `--fly`, temporary directory creation and recursive deletion, shutdown hook; stores unchanged.
- Runtime directory `tmp/` (removed per run).
