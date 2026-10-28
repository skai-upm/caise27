# Proposal

## Why

Milestone F1-C08 adds the general `HELP` and `EXIT` commands: users need to discover the available operations and to terminate the application in a controlled way, with the same behavior in interactive and file mode; after `EXIT` no further command may run.

## What Changes

- Add `HELP`: prints the available commands with their basic syntax — `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, `EXIT`.
- Add `EXIT`: prints a farewell line and terminates the application normally (exit status 0); in both modes no subsequent line is processed.
- Commit with the mandatory message `[F1-C08] Add help and exit commands`.

## Capabilities

### New Capabilities
- `execution-control`: The `HELP` and `EXIT` commands and the guarantee that nothing is executed after `EXIT`.

### Modified Capabilities
- None.

## Impact

- `CommandProcessor` (new commands and exit flag), `App` loop stops when exit is requested; tests.
- Ambiguities flagged:
  - The farewell text is not specified; `EXIT` prints `Goodbye` (exit status 0).
  - `HELP;x` / `EXIT;x` (with arguments) are rejected as wrong number of arguments; `HELP` lists the modifiable `<field>` names (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`) as extra guidance.
