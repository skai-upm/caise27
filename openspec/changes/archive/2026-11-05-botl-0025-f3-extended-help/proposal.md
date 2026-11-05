## Why

Delivery 3, milestone F3-C04 (source section 7), keeps `HELP` (syntax only) and adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose alongside its syntax. The class that generates help must receive, by dependency injection, the behaviour used to build its output. That keeps the design open to new help variants without coupling them to the logic that reads and executes commands.

Today the help text is a constant inside `CommandProcessor`, built from `HELP_SYNTAX`.

## What Changes

- New `presentation.CommandCatalog`: the ordered list of `CommandDescriptor(syntax, purpose)` entries for every recognized command. It becomes the single source for all help variants and now includes `HELP EXTENDED`.
- New strategy interface `HelpFormatter`, with two implementations:
  - `SyntaxHelpFormatter`: the current `HELP` text.
  - `ExtendedHelpFormatter`: syntax plus purpose.
- New `HelpGenerator`, which receives its `HelpFormatter` (and the catalog) through constructor injection.
- New `HelpCommands` registry, which maps help commands to generators. `HelpConfiguration` registers `HELP` and `HELP EXTENDED` at initialization.
- `CommandProcessor` receives the `HelpCommands` by injection and delegates any registered help command to it. It contains no help text or variant logic.
- `HELP` output is unchanged apart from listing `HELP EXTENDED`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `execution-control`:
  - The HELP list includes `HELP EXTENDED`.
  - A new `HELP EXTENDED` requirement.
  - A new requirement for injected help-generation behaviour.

## Impact

- `presentation`: new `CommandCatalog`, `CommandDescriptor`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`, `HelpCommands` and `HelpConfiguration`. `CommandProcessor` and `App` are adapted.
- Tests that pin the HELP list are updated, and help tests are added.
- README is updated.
- The UML model covers only the business and persistence packages, so it does not change.
