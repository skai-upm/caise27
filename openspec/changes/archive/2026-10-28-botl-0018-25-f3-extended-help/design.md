# Design

## Context

`HelpCommand` holds a static `SYNTAX` list and prints it. `CommandProcessor` registers commands by keyword (text before the first `;`), so `HELP EXTENDED` is a distinct keyword. Controllers are injected into commands by the processor (F3-C03).

## Decisions

- **`CommandHelp` record** (syntax, description) and **`CommandCatalog`** (`ENTRIES` in display order) in `presentation`; `HelpCommand.SYNTAX` is derived from the catalogue so existing checks keep working.
- **Strategy**: `HelpStrategy` interface `String render(List<CommandHelp>)`; `SyntaxHelpStrategy` ("Available commands:" + syntax lines, identical to today's output) and `ExtendedHelpStrategy` ("Available commands (extended):" + for each entry the syntax and an indented `    <description>` line).
- **`HelpGenerator`**: constructor-injected `HelpStrategy` and entries; `generate()` delegates. `HelpCommand(HelpGenerator)` prints `generate()`.
- **Wiring**: `CommandProcessor` registers `HELP` → `new HelpCommand(new HelpGenerator(new SyntaxHelpStrategy(), CommandCatalog.ENTRIES))` and `HELP EXTENDED` → same with `ExtendedHelpStrategy`. A future style is a new `HelpStrategy` implementation plus one registration.
