# Proposal

## Why

Every run writes to the permanent `data/` files, so trying command scripts or testing in the lab alters the real state. An isolated mode is needed that exercises all persistence features against throw-away files.

## What Changes

- New startup flag `--fly`. With it, the application creates at startup a fresh directory `tmp/<yyyyMMdd_HHmmss>/` (unique per execution) and every persistent file (users, groups, tickets, group tickets, attractions and any future one) is read from and written to that directory only.
- The directory and its files are deleted when the application terminates: end of input, `EXIT`, end of the command file, or a command-file error.
- `--fly` works in interactive mode and with a command file (`--fly input.txt`).
- Without `--fly` the permanent `data/` behavior is unchanged.
- No repository code is duplicated: storages are unchanged and only receive different paths from a single data-directory configuration.
- `tmp/` is git-ignored.

## Capabilities

### New Capabilities
- `temporary-persistence`: The `--fly` execution mode, its per-run temporary data directory and its cleanup.

### Modified Capabilities
- `command-processing`: mode selection now distinguishes the `--fly` flag from the command-file argument.

## Impact

- New `DataDirectory` class (permanent or temporary location; resolves file names; deletes itself when temporary). `App` parses arguments and builds all storages from it.
- `AppTest` updated to pass a data directory; `uml/model.puml` and `.gitignore` updated.
