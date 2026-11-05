# Proposal

## Why

Milestone F1-C08 (Delivery 1, step 10) adds the general `HELP` and `EXIT` commands (source §11, §12). `HELP` shows the available operations and their syntax. `EXIT` terminates the application in a controlled manner, and no later command may run after it, whether commands come from standard input or from a file. Today both are answered as unknown commands, and a file's lines after `EXIT` are still executed.

## What Changes

- Add `HELP`: prints the available commands with their basic syntax, covering at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.
- Add `EXIT`: prints a confirmation and stops command processing. No subsequent line, from standard input or a file, is read or executed, and the application terminates normally. Users are already persisted after each change, so no data is lost.
- Both behaviors apply through the shared command loop, so they are identical for both input sources.

In scope: `HELP`, `EXIT`, stopping processing after `EXIT`.

Out of scope: Delivery 2 commands and an extended help (Delivery 3 "extended help").

## Capabilities

### New Capabilities
- `execution-control`: The general `HELP` and `EXIT` commands — listing available commands, controlled termination, and no execution after `EXIT` for any input source.

### Modified Capabilities
- None. `command-interface` requirements (end of input terminates normally, identical processing for both sources) remain valid; `EXIT` is an additional way to terminate.

## Impact

- `CommandProcessor` (HELP/EXIT handling and an exit-requested state), `App.run` (stops the loop when exit is requested), tests.
