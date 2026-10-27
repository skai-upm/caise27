# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose next to its syntax, while `HELP` keeps showing the syntax. The help generator must receive the behaviour that builds its output through dependency injection, so new help variants can be added later without touching command reading/execution.

## What Changes

- Add a single catalogue of the commands the application recognises, each with its syntax and a short purpose.
- Add a help-generation component that receives an injected formatting behaviour; provide a syntax-only behaviour (used by `HELP`) and an extended behaviour (used by `HELP EXTENDED`).
- Wire both generators at start-up and inject them into the command processor, which only asks them for text.
- `HELP` output keeps the same command syntax lines (the separate USER UPDATE field hint moves into the extended explanation) and now also lists `HELP EXTENDED`.
- Commit with the exact message `[F3-C04] Add extended help generation`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `execution-control`: new `HELP EXTENDED` command and injectable help generation.

## Impact

- New `presentation.help` package (`CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`); `CommandProcessor` receives the generators; `Main` wires them.
- Ambiguity flagged: extended help layout is `<syntax>` followed by an indented `<purpose>` line per command.
