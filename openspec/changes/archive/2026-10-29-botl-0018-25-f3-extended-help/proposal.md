# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose next to its syntax, and requires the class that generates help text to receive the behavior that builds the output through dependency injection, so that new help variants can be added without coupling them to the logic that reads and executes commands.

## What Changes

- New command `HELP EXTENDED`: lists every command's syntax with a short description of its purpose.
- `HELP` keeps showing the syntax of the recognized commands (output unchanged) and now also lists `HELP EXTENDED`.
- A command catalog holds each command's syntax and description; a `HelpGenerator` produces help text using an injected `HelpFormatter` strategy (syntax-only or extended). The application initialization creates both generators and injects them into the command processor.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `command-interface`: adds HELP EXTENDED and the injected help-generation behavior.

## Impact

- New presentation classes (`CommandCatalog`, `CommandInfo`, `HelpGenerator`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`); `CommandProcessor` and `App` wiring; tests; UML.

## Ambiguities (flagged)

- The description texts are not given by the source; short purpose sentences derived from each command's specification are used.
- USER commands are listed (as required by HELP) but are implemented by later proposals in this batch; their descriptions describe the specified purpose.
