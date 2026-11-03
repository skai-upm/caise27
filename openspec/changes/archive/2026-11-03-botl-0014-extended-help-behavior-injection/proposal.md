# Proposal

## Why

`HELP` only lists command syntax; users also need a short explanation of what each command does. The help construction must be open to new presentation formats without touching command processing.

## What Changes

- `HELP` keeps its current output (syntax of every command).
- New `HELP EXTENDED`: syntax plus a brief description of every command.
- One registry of the supported commands (`CommandCatalog` of `CommandInfo` syntax + description) feeds both help modes.
- `HelpGenerator` builds the help text with a `HelpFormatter` behaviour injected through its constructor (`SyntaxHelpFormatter`, `ExtendedHelpFormatter`); the composition root creates and injects the generators into `CommandProcessor`.
- No change to any other command.

## Capabilities

### New Capabilities
- `extended-help`: the `HELP EXTENDED` command, the command registry used by both help modes and the injected help-formatting behaviour.

### Modified Capabilities
- `application-control`: HELP lists the new `HELP EXTENDED` command.

## Impact

- New `presentation.help` package; `CommandProcessor` delegates HELP/HELP EXTENDED to injected generators; `App` wiring; `uml/model.puml`.
