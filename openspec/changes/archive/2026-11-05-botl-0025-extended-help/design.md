# Design

## Context

`CommandProcessor` builds the HELP text itself from its registry (`usage`, `note`).

## Goals / Non-Goals

**Goals:** Strategy-based help formats injected into a generator. **Non-Goals:** localisation, paging.

## Decisions

- **`presentation.help.CommandInfo`** (record: syntax, note, purpose) — the processor exposes its catalogue as `List<CommandInfo>` in registration order.
- **`HelpFormat`** (strategy interface: `String format(List<CommandInfo>)`); `SyntaxHelpFormat` reproduces the current HELP output exactly; `ExtendedHelpFormat` prints `syntax` then an indented `- purpose` line.
- **`HelpGenerator`** receives a `HelpFormat` in its constructor (constructor injection) and generates the text from the catalogue. `ApplicationInitializer` creates the two generators and injects them into `CommandProcessor` (constructor parameters), which registers `HELP` and `HELP EXTENDED` delegating to them.
- Purpose texts are kept in one table in `CommandProcessor`, keyed by command name, and attached at registration so the catalogue stays the single source.

## Risks / Trade-offs

- [Constructor parameter list grows] → acceptable; the composition root is the only caller.
