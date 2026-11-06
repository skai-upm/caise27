# Design

## Context

See proposal.md – Why. `CommandProcessor` holds a hard-coded `HELP_TEXT` string and a table of command handlers; `Main` builds `new CommandProcessor(park)`.

## Goals / Non-Goals

**Goals:**
- Strategy-based help formatting injected into a generator; processor depends only on the generators.
- `HELP` output unchanged apart from the new `HELP EXTENDED` line.

**Non-Goals:**
- Localization or dynamic catalogs.

## Decisions

- **`CommandInfo`** (record): `syntax`, `purpose`, optional `note` (e.g. the `<field>` list under `USER UPDATE`).
- **`CommandCatalog.commands()`**: the ordered list of all commands, single source for both help variants.
- **`HelpFormatter`** (strategy interface): `String format(List<CommandInfo>)`. **`SyntaxHelpFormatter`** reproduces the existing `HELP` layout; **`ExtendedHelpFormatter`** prints each syntax followed by an indented `- <purpose>` line (and the note).
- **`HelpGenerator`**: constructor injection of `(HelpFormatter, List<CommandInfo>)`; `generate()`.
- **`CommandProcessor(ParkController, HelpGenerator basic, HelpGenerator extended)`** — constructor injection; the existing `CommandProcessor(ParkController)` delegates with the default generators (keeps callers/tests simple). `HELP` and `HELP EXTENDED` are entries in the command table taking 0 fields.
