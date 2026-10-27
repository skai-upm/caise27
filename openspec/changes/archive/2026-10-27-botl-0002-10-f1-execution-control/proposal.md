# Proposal

## Why

Milestone F1-C08 requires the general `HELP` and `EXIT` commands: `HELP` shows the available operations and their syntax, `EXIT` ends the application in a controlled way, and both must behave the same whether commands come from standard input or from a file, with `EXIT` preventing any later command from running. The project currently only contains the quickstart skeleton, so a minimal command loop is needed for these commands to be usable at all.

## What Changes

- Add a single command-processing path shared by both input sources (standard input and a command file given as the first argument).
- Add the `HELP` command, which prints the available commands and their basic syntax, including at least `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT` as listed in the source.
- Add the `EXIT` command, which terminates the application in a controlled manner; no command after `EXIT` is executed (in file mode no later line is processed).
- Unrecognised input is reported with a controlled error message instead of crashing (needed so the loop keeps running; the USER commands themselves belong to later milestones).
- Record the mandatory milestone commit `[F1-C08] Add help and exit commands` once implementation is verified.

## Capabilities

### New Capabilities
- `command-processing`: How the application reads commands (standard input or a command file), dispatches them through one shared processor, and the general `HELP` / `EXIT` commands.

### Modified Capabilities
<!-- None. project-build is unaffected. -->

## Impact

- `App` (entry point) gains a command loop; new command-processing classes under `es.upm.amusementpark`.
- New unit tests for HELP, EXIT and the shared stdin/file processing.
- Out of scope (later milestones): executable JAR manifest (F0-C02), USER commands (F1-C02..F1-C06), full file-mode error handling (F1-C07).
