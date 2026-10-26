# Design

## Context

`CommandProcessor` holds `USAGE_*` strings and a `HELP_LINES` list; HELP prints them. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One catalog of commands; Strategy for help formats; generators injected into command processing.

**Non-Goals:**
- Localisation, per-command help (`HELP USER CREATE`).

## Decisions

- **`CommandInfo`** record `(syntax, description)`; **`CommandCatalog`** exposes one public constant per command (used by `CommandProcessor` for usage errors, so usage and help cannot drift) and `all()` returning them in display order.
- **Strategy `HelpFormatter`** `String format(List<CommandInfo>)`: `SyntaxHelpFormatter` ("Available commands:" + syntax lines, same layout as today) and `ExtendedHelpFormatter` (syntax line + indented description).
- **`HelpGenerator`** `(List<CommandInfo> commands, HelpFormatter formatter)` with `generate()`: the class that builds help text, with both dependencies injected (constructor injection — it is not a singleton).
- **Injection into `CommandProcessor`**: constructor `(ParkController, Map<String, HelpGenerator>)`, keys = help mode (`""` for `HELP`, `"EXTENDED"` for `HELP EXTENDED`). `App.configure` builds the map (the composition root). Parsing: operation `HELP` with zero or one extra word selects the mode; unknown mode → error.
- **Plain HELP output**: identical layout, except the `USER UPDATE` line no longer appends `(Campo: [...])`; editable fields move to its extended description.

## Risks / Trade-offs

- [Constructor signature change of `CommandProcessor`] → only App and tests construct it; tests get the map from `App.helpGenerators()`.
