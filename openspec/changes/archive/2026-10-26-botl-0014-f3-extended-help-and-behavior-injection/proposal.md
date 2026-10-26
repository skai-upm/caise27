# Proposal

## Why

`HELP` only lists command syntax, and the help text is hard-coded inside the command processor, so any new presentation of help would mean editing general command processing. Delivery 3 asks for an extended help and for the help-building behavior to be injected so new formats can be added later.

## What Changes

- `HELP` keeps printing the concise list of command syntaxes (unchanged output).
- New `HELP EXTENDED` prints each command's syntax and a brief explanation of its behavior.
- All commands are registered once in a command catalog (operation, syntax, description); both help modes and the processor's usage messages read from it.
- The help generator receives the formatting behavior (one formatter per help mode) through dependency injection at startup; it does not instantiate its formatters. Adding a new format means registering a new formatter, without touching command processing.
- PlantUML model updated.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `command-processing`: HELP is defined from the registered commands; adds HELP EXTENDED and pluggable help formats.

## Impact

- New package `es.upm.parque.presentation.help` (`CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`); `CommandProcessor` and `App` updated.
