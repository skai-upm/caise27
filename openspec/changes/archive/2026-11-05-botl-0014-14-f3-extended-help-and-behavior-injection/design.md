# Design

## Context

`presentation.CommandProcessor` holds a hard-coded `COMMAND_SYNTAX` list and a private `help()` method; `App` creates `new CommandProcessor(controller)`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One registry of command information; help construction parameterized by an injected strategy; command processing only delegates.

**Non-Goals:**
- Other formats (Markdown, per-command help), localization.

## Decisions

- **`CommandInfo`** record (`syntax`, `description`) and **`CommandCatalog`** holding the ordered list of registered commands (`commands()`); `CommandCatalog.standard()` returns the application's commands. The concise HELP keeps the existing suffix `(Campo: NAME, ...)` of `USER UPDATE` inside the syntax text, so the current HELP lines are unchanged.
- **Strategy** `HelpFormatter` interface (`String format(List<CommandInfo>)`); `SyntaxHelpFormatter` prints `Available commands:` and one syntax per line (identical to today); `ExtendedHelpFormatter` prints `Available commands (extended):` and, per command, the syntax line followed by an indented description line.
- **`HelpGenerator`** receives `CommandCatalog` and `HelpFormatter` in its constructor (constructor injection) and exposes `generate()`; it never creates its collaborators.
- **`CommandProcessor`** receives `basicHelp` and `extendedHelp` (`HelpGenerator`s) together with the controller; `HELP` → `basicHelp.generate()`, `HELP EXTENDED` → `extendedHelp.generate()`. It does not know about formatters.
- **Wiring** in `App.createProcessor(ParkController)` (presentation startup): one catalog, two generators with the two formatters. Tests use the same factory or inject a custom formatter.
- **Observable change**: `HELP` gains the line `HELP EXTENDED`; everything else is unchanged (checked by diffing the E1/E2 script output against the previous JAR, expecting only that line).

## Risks / Trade-offs

- [Descriptions drifting from behavior] → descriptions are short and kept next to the syntax in the single catalog.
