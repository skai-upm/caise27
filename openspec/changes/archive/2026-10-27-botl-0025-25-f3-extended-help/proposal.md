# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which explains the purpose of every command next to its syntax, and requires the help generator to receive the behaviour that builds its output through dependency injection, so new help variants can be added without touching command reading and execution.

## What Changes

- `HELP EXTENDED` shows, for every command, its syntax and a brief explanation of its purpose.
- `HELP` keeps showing the syntax of all recognised commands (now also listing `HELP EXTENDED`).
- Help text is produced by a help generator that receives its formatting behaviour (a strategy) by injection; HELP and HELP EXTENDED use the same command catalogue with different injected formatters.
- Record the mandatory milestone commit `[F3-C04] Add extended help generation` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: adds HELP EXTENDED and the injected help-generation behaviour.

## Impact

- New `presentation.help` package (`CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`); `CommandProcessor` receives its help generators; UML updated.
