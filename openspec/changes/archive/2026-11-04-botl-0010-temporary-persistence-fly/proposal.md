# Proposal

## Why

Every run writes to the permanent data files (`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`). Trying commands or running test scripts therefore alters the real state. A `--fly` mode must give an isolated, throw-away persistence location for one run, without duplicating any repository logic.

## What Changes

- New launch option `--fly`, given as the first argument and optionally followed by a command-file path:
  - `java -jar target/<jar> --fly`
  - `java -jar target/<jar> --fly input_fase2.txt`
- At startup in `--fly` mode the application creates `tmp/<yyyyMMdd_HHmmss>/` (a timestamp per run) and points **all** repositories at files inside it, through the same path configuration used normally. The run starts with no stored data.
- When the run ends (EXIT, end of input or file, or an error reading the command file), the temporary directory and all its files are deleted.
- Without `--fly`, the behaviour and file locations are exactly as before.

## Capabilities

### New Capabilities
- `temporary-persistence-mode`: the `--fly` option, the per-run temporary data directory, isolation from permanent data, and cleanup at termination.

### Modified Capabilities
- `command-input-modes`: interactive and file mode are selected by the arguments that remain after an optional leading `--fly`.
- `system-date`: the rule restricting reads of the real clock also allows the technical timestamp that names the `--fly` directory. No business rule uses it.

## Impact

- `App` only: argument parsing, data-directory selection and cleanup. Repositories and managers are unchanged. They already receive their file paths, which are derived from one data location.
- `tmp/` is added to `.gitignore`.
