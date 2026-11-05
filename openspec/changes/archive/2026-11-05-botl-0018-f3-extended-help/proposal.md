# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which shows each command's syntax with a brief explanation of its purpose. `HELP` must keep showing the syntax list. The class that generates help must receive, through dependency injection, the behavior used to build the output, so that new help variants can be added later without touching command reading or execution. Today the help text is a constant inside `CommandProcessor`, the class that reads and dispatches commands.

## What Changes

- Add `HELP EXTENDED`, which lists every available command with its syntax and a one-line purpose.
- Keep `HELP` output exactly as today (the Delivery 2 syntax list plus the invoice commands, now also listing `HELP EXTENDED`).
- Move help out of `CommandProcessor` into a `presentation.help` package:
  - `CommandCatalog` describes each command (syntax and purpose).
  - `HelpFormatter` is the injected strategy, implemented by `SyntaxHelpFormatter` and `ExtendedHelpFormatter`.
  - `HelpGenerator` receives its formatter and catalog through its constructor.
  - `HelpCommands` registers `HELP` and `HELP EXTENDED` with generators configured at initialization.
- `CommandProcessor` keeps only reading, dispatch, `EXIT` and error handling.

### Out of scope
- Localized or alternative help formats (the design allows adding them as new formatters).

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: adds `HELP EXTENDED` and help generation through an injected formatting behavior.

## Impact

- New `presentation.help` package.
- `CommandProcessor` loses `HELP_TEXT`.
- `App.registerCommands` registers help.
- Tests that relied on the built-in `HELP` register help explicitly.
- UML updated.
