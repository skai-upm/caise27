# Design

## Context

`CommandProcessor` keeps `COMMAND_SYNTAX` (a list of strings) and builds the HELP text itself in `help()`. `App.createProcessor` is the composition root. See proposal.md.

## Goals / Non-Goals

**Goals:** single source of command information, extended help, formatting behaviour injected (Strategy + constructor injection).

**Non-Goals:** other formats (e.g. HTML) now, localisation, changing other commands.

## Decisions

- **Registry:** `CommandInfo(syntax, description)` and `CommandCatalog.COMMANDS` (unmodifiable list, current order plus `HELP EXTENDED` after `HELP`). `CommandProcessor.COMMAND_SYNTAX` is derived from it, so syntax lists cannot diverge. A test processes the operation of every registered syntax and checks it is not "Unknown command" (keeps registry and dispatcher synchronised).
- **Strategy:** interface `HelpFormatter { String format(List<CommandInfo> commands); }`; `SyntaxHelpFormatter` reproduces today's text exactly (`Available commands:` + indented syntaxes); `ExtendedHelpFormatter` prints `Available commands (extended):` and, for each command, the syntax line and an indented description line.
- **Injection:** `HelpGenerator(List<CommandInfo> commands, HelpFormatter formatter)` with `generate()`; it creates nothing. `App` builds `new HelpGenerator(CommandCatalog.COMMANDS, new SyntaxHelpFormatter())` and the extended one and passes both to `new CommandProcessor(park, help, extendedHelp)`. `CommandProcessor` only dispatches `HELP` / `HELP EXTENDED` to them (no parameters allowed).
- Package `presentation.help`: help is presentation, independent of business rules.

## Risks / Trade-offs

- [Descriptions must be maintained when commands change] → they sit next to the syntax in one registry entry and the synchronisation test fails if a registered command is not dispatched.
