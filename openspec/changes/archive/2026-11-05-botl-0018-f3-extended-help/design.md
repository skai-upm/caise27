# Design

## Context

`CommandProcessor` holds `HELP_TEXT` and registers `HELP` and `EXIT` in its constructor. The Delivery 2 help list is mandated verbatim (`execution-control` HELP requirement). See proposal.md - Why.

## Decisions

- **Strategy for formatting**: `interface HelpFormatter { String format(List<CommandDescription>); }`.
  - `SyntaxHelpFormatter` reproduces the current output byte for byte (`Available commands:` followed by `  <syntax>` lines).
  - `ExtendedHelpFormatter` prints `Available commands (extended):`, then `  <syntax>` and `      <purpose>` per command.
- **`HelpGenerator(HelpFormatter, CommandCatalog)`**: the formatter is injected through the constructor. The generator only asks the catalog for descriptions and delegates formatting.
- **`CommandCatalog`**: an ordered, immutable list of `CommandDescription(syntax, purpose)` records. `CommandCatalog.standard()` holds the mandated list (including USER commands, which the source requires `HELP` to document), the invoice commands and `HELP EXTENDED`.
- **`HelpCommands.register(processor, plainGenerator, extendedGenerator, out)`** is called by the composition root (`App.registerCommands`). `registerDefault(processor, out)` builds the standard generators for tests and simple wiring.
- **`CommandProcessor`** keeps `EXIT` (session control) but no longer knows anything about help.
