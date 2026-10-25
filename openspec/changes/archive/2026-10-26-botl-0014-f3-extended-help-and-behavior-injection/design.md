# Design

## Context

`CommandProcessor` holds `COMMAND_SYNTAX` (static list) and builds `HELP` itself. `Bootstrap` is the composition root and the processor constructor already has 6 parameters.

## Goals / Non-Goals

**Goals:**
- Strategy for help formatting, injected into the help generator.
- One registry of commands (syntax + description) used by every help mode.

**Non-Goals:**
- Changing any other command or the concise HELP output.

## Decisions

- **`CommandInfo`** (syntax, description) and **`CommandRegistry`** (ordered list; `CommandRegistry.standard()` builds the park's commands with their descriptions, including `HELP EXTENDED`).
- **Strategy**: interface `HelpFormat { String format(List<CommandInfo> commands); }` with `SyntaxHelpFormat` (one syntax per line, the current HELP output) and `ExtendedHelpFormat` (syntax line + `    ` + description).
- **`HelpGenerator(CommandRegistry registry, HelpFormat format)`**: `String generate()`; receives both collaborators by constructor injection, never creates formats itself.
- **Help modes by name**: `CommandProcessor` receives `Map<String, HelpGenerator> helpModes` (keys are upper-case command names, e.g. `HELP`, `HELP EXTENDED`) built in `Bootstrap`; `process` looks the operation up in this map before the `switch`. Adding a format is a `Bootstrap` change only. This also keeps the constructor at 7 parameters.
- **Packages**: all help classes live in `presentation` (they produce user-facing text).
- **Tests**: `HelpGeneratorTest` with a stub format proves injection; registry consistency test (every registered syntax is recognised by the processor) replaces the one based on `COMMAND_SYNTAX`.

## Risks / Trade-offs

- [Registry and switch could drift] → The consistency test fails if a registered command is unknown to the processor.
