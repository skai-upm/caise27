# Design

## Context

`CommandProcessor` (presentation) holds one syntax constant per command, a `COMMAND_SYNTAX` list for `HELP`, and builds the help text itself. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Single registry of commands; Strategy-based help formatting injected from `App`.

**Non-Goals:**
- Turning commands into Command objects; localisation.

## Decisions

- **Package `presentation.help`**:
  - `CommandInfo` record (`operation`, `syntax`, `description`).
  - `CommandCatalog`: ordered list of `CommandInfo`, `syntaxOf(operation)`, and `standard()` factory with all current commands (including `HELP`, `HELP EXTENDED`, `EXIT`).
  - `HelpFormatter` (Strategy interface) `String format(List<CommandInfo>)`; `SyntaxHelpFormatter` (current `HELP` text, byte-identical) and `ExtendedHelpFormatter` (`<syntax>` then `    <description>`).
  - `HelpGenerator(CommandCatalog, Map<String, HelpFormatter>)`: `generate(mode)` picks the formatter by mode (`""` for `HELP`, `"EXTENDED"`), `modes()` for error messages, `getCatalog()`.
- **CommandProcessor** receives the `HelpGenerator` by constructor; the operation `HELP` or `HELP <mode>` is routed to `generate(mode)`; usage errors use `catalog.syntaxOf(operation)`, removing the duplicated syntax constants. Alternative: a `HELP;EXTENDED` parameter — rejected, the epic names the command `HELP EXTENDED`.
- **App** builds the catalog, the two formatters and the generator, and injects them. Alternative: generator constructing its formatters — rejected by the epic.
- **UML**: help package with `HelpFormatter` realized by the two formatters; `HelpGenerator x--> CommandCatalog : catalog`, `HelpGenerator x--> "0..*" HelpFormatter : formatters`, `CommandCatalog x--> "0..*" CommandInfo : commands`, `CommandProcessor x--> HelpGenerator : helpGenerator`.

## Risks / Trade-offs

- [`CommandProcessor` constructor reaches 7 parameters] → acceptable for the composition root wiring; further growth should group controllers.
