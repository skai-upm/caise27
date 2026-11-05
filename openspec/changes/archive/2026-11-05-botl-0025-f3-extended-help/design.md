## Context

`CommandProcessor` holds `HELP_SYNTAX` (22 entries) and a `HELP_TEXT` constant: the syntax list plus a footer explaining the `USER UPDATE` fields. Dispatch is a `switch` on the first element. `App` creates `new CommandProcessor()`. Tests pin `CommandProcessor.HELP_SYNTAX` and its size.

## Goals / Non-Goals

**Goals:**
- `HELP EXTENDED` with a purpose for every command.
- Help behaviour injected into the generator (Strategy plus constructor injection).
- Help variants decoupled from command reading and execution.

**Non-Goals:**
- Localization.
- Per-command help (such as `HELP <command>`).
- Changes to the UML of the business layer.

## Decisions

1. **Catalog.**
   - `CommandDescriptor` is a record `(String syntax, String purpose)`.
   - `CommandCatalog.COMMANDS` is an immutable ordered list. It keeps the current order and adds `HELP EXTENDED` right after `HELP`, so `EXIT` stays last.
   - `CommandCatalog.syntaxes()` gives the syntax list, which replaces `HELP_SYNTAX` in the tests.
   - Later milestones (passes, attraction types) add entries only here.
2. **Strategy.**
   - The `HelpFormatter` interface is `String format(List<CommandDescriptor> commands)`.
   - `SyntaxHelpFormatter` reproduces the current text exactly: "Available commands:", indented syntaxes, and the `USER UPDATE` field footer.
   - `ExtendedHelpFormatter` prints "Available commands (extended):", then each syntax on its own line followed by an indented line with its purpose.
3. **Generator.**
   - `HelpGenerator(List<CommandDescriptor> catalog, HelpFormatter formatter)`; `generate()` returns `formatter.format(catalog)`.
   - The generator never chooses a formatter itself.
4. **Registry and wiring.**
   - `HelpCommands` wraps a `Map<String, HelpGenerator>` that preserves insertion order.
   - `handles(command)` tells whether a command is a help command.
   - `respond(elements)` validates that the command has no extra fields (otherwise `InvalidCommandException("<cmd> takes no fields")`) and returns the generated text.
   - `HelpConfiguration.standard()` builds the registry with `HELP` → syntax formatter and `HELP EXTENDED` → extended formatter. This is the presentation counterpart of `ParkConfiguration`.
5. **CommandProcessor.**
   - Constructor `CommandProcessor(HelpCommands help)`. The no-arg constructor delegates to `HelpConfiguration.standard()`, which keeps tests and `App` simple; `App` uses the injecting constructor explicitly.
   - `dispatch` first asks `help.handles(command)`, so new help variants need no change to dispatch.
   - `HELP_SYNTAX` and `HELP_TEXT` are removed from `CommandProcessor`.

## Risks / Trade-offs

- Purposes must stay in sync with the commands. Mitigation: a test checks that every catalog entry has a non-empty purpose and that every dispatch command prefix appears in the catalog.
