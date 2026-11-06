# Proposal

## Why

Milestone F1-C08 (Delivery 1, step 10) adds the general `HELP` and `EXIT` commands: `HELP` lists the available operations and their syntax, and `EXIT` ends the application in a controlled way so that no later command runs, consistently in interactive and file mode. Today both are reported as unknown commands.

## What Changes

- `HELP` prints the available commands with their basic syntax, at least: `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>` (with the modifiable fields), `HELP`, `EXIT`.
- `EXIT` prints a closing message and stops command processing; in either input mode no subsequent line is processed and the application ends normally.

### In scope
- Source sections 11 and 12.

### Out of scope
- Delivery 1 integration hardening (F1-C09).

## Capabilities

### New Capabilities
- `execution-control`: the `HELP` and `EXIT` commands and their consistent behavior across input sources.

### Modified Capabilities
<!-- None: command-interface requirements (input sources, shared processing, incorrect commands) remain valid unchanged. -->

## Impact

- `CommandProcessor` handles `HELP`/`EXIT` and exposes whether exit was requested; the shared loop in `Main` stops on it. New tests.
