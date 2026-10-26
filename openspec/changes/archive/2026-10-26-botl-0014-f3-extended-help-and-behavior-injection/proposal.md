# Proposal

## Why

HELP only lists syntaxes and its text is a hard-coded list inside `CommandProcessor`, duplicated with the usage strings of parameter errors. Users need a mode that also explains what each command does, and the help output must be able to gain new presentation formats without touching command processing.

## What Changes

- `HELP` keeps printing the syntax of every registered command.
- New `HELP EXTENDED` prints each command's syntax followed by a brief description of its behavior.
- A single catalog of registered commands (syntax + description) feeds both help modes and the usage shown in parameter errors.
- The help text is built by a help generator that receives, by dependency injection, the formatting behavior (a strategy): syntax-only or extended. New formats are added by registering another formatter at startup, without changing command processing.
- `CommandProcessor` receives the configured help generators (keyed by help mode) instead of building help itself.
- Required commit title: `[F3-C04] Add extended help generation`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `command-processing`: HELP is generated from the registered-command catalog; new `HELP EXTENDED` mode; help formatting behavior injected.

## Impact

- New presentation classes: `CommandInfo` (record), `CommandCatalog`, `HelpFormatter` (interface), `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`; `CommandProcessor` constructor takes the help generators; `App.configure` wires them.
- The `USER UPDATE` line of plain HELP no longer appends the editable field list (it moves to the extended description).
