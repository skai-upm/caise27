# Design

## Context

`CommandProcessor.help()` concatenates `USAGE` and the `UserField` names. `App.run` builds `new CommandProcessor(park)`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- One command registry; help formatting as an injected strategy; byte-compatible concise help (plus the new `HELP EXTENDED` entry).

**Non-Goals:**
- New output formats beyond the two required, localisation, changing command dispatch.

## Decisions

- **`CommandInfo(String syntax, String description)`** record and **`CommandCatalog.commands()`**, an immutable list in the current HELP order with `HELP EXTENDED` inserted after `HELP`. Descriptions are one sentence; the `USER UPDATE` description lists the `UserField` names.
- **Strategy:** `interface HelpFormatter { String format(List<CommandInfo> commands); }`
  - `SyntaxHelpFormatter` reproduces the existing text: `OK: available commands:`, `  <syntax>` lines, `  Campo (USER UPDATE): NAME, ...`.
  - `ExtendedHelpFormatter` prints `OK: available commands (extended):` then, per command, `  <syntax>` and `      <description>`.
- **`HelpGenerator(List<CommandInfo> commands, HelpFormatter formatter)`** with `generate()`. It does not create its formatter or commands; it receives them.
- **Wiring:** `CommandProcessor(ParkController park, HelpGenerator help, HelpGenerator extendedHelp)`; `App.run` creates both generators from `CommandCatalog.commands()`, the presentation composition root. Tests use a small factory method that wires the same way.
- **Dispatch:** a new `case "HELP EXTENDED"` with the zero-parameter check. A `CommandCatalogTest` checks that every catalog entry's operation is recognised by the processor (no `unknown command` reply), keeping registry and dispatch consistent.

## Risks / Trade-offs

- [HELP output gains a line] → Required: `HELP EXTENDED` is a new available command; all other lines are unchanged.
