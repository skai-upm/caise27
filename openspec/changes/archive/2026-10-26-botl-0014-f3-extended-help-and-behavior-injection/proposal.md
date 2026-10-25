# Proposal

## Why

`HELP` only prints command syntax, and the list of syntaxes is a constant inside the command processor. Users need a mode that also explains what each command does, and the help construction must be prepared for future presentation formats without touching general command processing — which calls for injecting the formatting behavior into the help generator.

## What Changes

- `HELP` keeps showing the concise syntax list, unchanged.
- New `HELP EXTENDED` shows each command's syntax followed by a brief description of its behavior.
- Both modes are generated from the same registry of registered commands (syntax + description) instead of a hard-coded string list.
- The help generator receives the formatting behavior (a `HelpFormat` strategy) through dependency injection; the concise and extended formats are two implementations configured at startup, and help modes are registered by command name, so new formats can be added without changing the command processor.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: "HELP command" is based on the command registry; new requirements for `HELP EXTENDED` and for injected help formats.

## Impact

- New presentation classes `CommandInfo`, `CommandRegistry`, `HelpFormat`, `SyntaxHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`; `CommandProcessor` receives the help modes; `Bootstrap` wires them; tests and UML updated.
