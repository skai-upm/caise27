# Design

## Context

`CommandProcessor` keeps a list of syntax strings (`COMMAND_SYNTAX`) used to validate parameter counts and to print HELP inline. Its constructor already takes six controllers; Sonar's parameter limit is seven.

## Goals / Non-Goals

**Goals:**
- Strategy pattern for help presentation, injected through the generator's constructor.
- One registry of commands (syntax + description) for validation and both help modes.

**Non-Goals:**
- Localisation, paging, per-command help.

## Decisions

- **Package `presentation.help`**:
  - `CommandInfo` record (`syntax`, `description`) with `operation()` (text before the first `;`) and `parameterCount()`.
  - `HelpFormatter` interface (`String format(List<CommandInfo> commands)`) — the injected behaviour.
  - `SyntaxHelpFormatter`: one syntax per line (same text as the current HELP).
  - `ExtendedHelpFormatter`: each syntax followed by an indented description line.
  - `HelpGenerator`: constructor `(List<CommandInfo> commands, HelpFormatter formatter)`; `generate()` returns `formatter.format(commands)`. It creates nothing itself.
- **Registry**: `CommandProcessor.COMMANDS` — public `List<CommandInfo>` replacing `COMMAND_SYNTAX`, with an entry for `HELP EXTENDED`. `syntaxOf` becomes `commandOf(operation)` and parameter checks use `CommandInfo.parameterCount()`.
- **Injection into the processor**: `CommandProcessor` receives `Map<String, HelpGenerator> helpGenerators` keyed by operation (`HELP`, `HELP EXTENDED`), keeping the constructor at seven parameters. The HELP and HELP EXTENDED handlers print `OK Available commands:` / `OK Available commands (extended):` followed by the generator's text.
- **Composition**: `App.configure` builds both generators from `CommandProcessor.COMMANDS` with the two formatters.
- **UML**: new package with the strategy interface realised by both formatters, `HelpGenerator` associations `commands` (→ `CommandInfo`, `0..*`) and `formatter` (→ `HelpFormatter`, `1`), and `CommandProcessor` → `HelpGenerator` (`helpGenerators`, `0..*`).

## Risks / Trade-offs

- [Descriptions can drift from behaviour] → Each description is a short statement of the command's effect, checked by review; a test enforces that none is empty.
