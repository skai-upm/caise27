# Design

## Context

`CommandProcessor` (presentation) owns `COMMAND_SYNTAX` (a list of strings) and a private `help()` concatenating them. `ApplicationContext` (composition root) wires business components; `App` creates `new CommandProcessor()`.

## Goals / Non-Goals

**Goals:** one registry of commands with descriptions; Strategy for help formatting injected into a help generator; command processing unaware of the format.

**Non-Goals:** other formats (HTML, per-command help) now; changing any other command.

## Decisions

- **Registry**: `CommandInfo` (record: `syntax`, `description`) and `CommandCatalog` (`List<CommandInfo> commands()`, `CommandCatalog.standard()` with the application's commands in the current HELP order plus `HELP EXTENDED` after `HELP`).
- **Strategy**: `interface HelpFormatter { String format(List<CommandInfo> commands); }`.
  - `SyntaxHelpFormatter` → `Available commands:` + `  <syntax>` lines (identical to today's HELP).
  - `ExtendedHelpFormatter` → `Available commands (extended):` + for each command `  <syntax>` and `      <description>`.
- **Context**: `HelpGenerator(CommandCatalog catalog, HelpFormatter formatter)` with `generate()`; it does not create its dependencies.
- **Injection**: `CommandProcessor(ParkController controller, HelpGenerator help, HelpGenerator extendedHelp)` is the main constructor; `ApplicationContext.commandProcessor()` builds the catalog, formatters and generators and injects them. The one-argument constructor remains as a convenience for tests and delegates with `CommandCatalog.standard()` and the two standard formatters (wiring only, no help logic).
- **Dispatch**: operations `HELP` and `HELP EXTENDED` (zero parameters) return `"OK: " + generator.generate()`.

## Risks / Trade-offs

- [Descriptions drift from behaviour] → kept next to the syntax in the catalog; a test checks every catalog syntax's operation is recognised by `CommandProcessor` (not "Unknown command").
