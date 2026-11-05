# Proposal

## Why

Milestone F1-C08 adds the general `HELP` and `EXIT` commands. Users currently have no in-application description of the available commands, and the only way to stop is to end the input; in file mode, every line is always processed.

## What Changes

- `HELP` displays the available commands with their basic syntax: `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, `EXIT`.
- `EXIT` terminates the application in a controlled manner (exit status 0); no subsequent command is executed, in interactive or file mode.
- Both behave identically for both input sources (same processing path).
- Out of scope: Delivery 1 integration hardening (F1-C09).

## Capabilities

### New Capabilities
- `execution-control`: The general `HELP` and `EXIT` commands.

### Modified Capabilities
<!-- None: existing commands, input modes and end-of-input termination are unchanged. -->

## Impact

- `CommandProcessor` gains HELP/EXIT handling and an exit-requested state; `InteractiveSession` stops reading once exit is requested.
