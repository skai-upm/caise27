# Design

## Context

HELP text is a constant in `CommandProcessor`, mixing the command list with command dispatch.

## Decisions

- **Strategy + injection**: `HelpFormatter` interface (`String format(List<CommandInfo>)`); `SyntaxHelpFormatter` prints `Available commands:` and one syntax per line; `ExtendedHelpFormatter` prints `Available commands (extended):` then, per command, the syntax and an indented purpose. `HelpGenerator(CommandCatalog, HelpFormatter)` gets both by constructor injection and exposes `generate()`.
- **`CommandCatalog`**: ordered immutable list of `CommandInfo(syntax, purpose)` — the single source of the recognised commands for help.
- **Wiring**: `CommandProcessor(HelpGenerator help, HelpGenerator extendedHelp)`; `Main` creates the generators. A parameterless constructor delegating to the default wiring is kept for tests and simple use.
- Dispatch adds `case "HELP EXTENDED"`.
