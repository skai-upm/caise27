# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose next to its syntax, while `HELP` keeps showing the syntax. The component that generates help text must receive, by dependency injection, the behavior used to build its output, so new help formats can be added later without touching command reading and execution. Today the help text is a hard-coded string inside the command processor.

## What Changes

- Describe every command once (syntax plus brief purpose) in a command catalog in the presentation layer.
- Add a help generator that builds help text from the catalog with an injected formatting behavior. One behavior lists syntax only and produces exactly the current `HELP` output. Another adds each command's purpose for `HELP EXTENDED`.
- Add the `HELP EXTENDED` command, and list it in both help outputs.
- Wire the two help generators into the command processor during application configuration. The processor only delegates to them and knows nothing about formats.

### In scope
- Command catalog, help formatting strategies, help generator, HELP EXTENDED, configuration wiring.

### Out of scope
- New help formats beyond basic and extended (the design only allows them). Changing the HELP syntax lines.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-control`: adds the HELP EXTENDED requirement and the requirement that help generation receives its formatting behavior by injection.

## Impact

- New `es.upm.park.presentation.help` package (`CommandDescription`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`).
- `CommandProcessor` delegates HELP / HELP EXTENDED to the injected generators. `AppConfiguration` wires them.
- Tests and UML diagram.
