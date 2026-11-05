# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose next to its syntax, while `HELP` keeps showing the syntax. The help-producing class must receive the output-building behavior through dependency injection, so new help variants can be added without touching command reading/execution.

## What Changes

- New command `HELP EXTENDED`: every available command with its syntax and a brief purpose.
- `HELP` unchanged (syntax list).
- Help generation extracted from `CommandProcessor` into a help generator that receives a help format strategy by injection (syntax-only and extended formats), wired in the composition root.
- Each command registration carries its purpose text.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: adds `HELP EXTENDED` and the injected help-generation behavior.

## Impact

- New `presentation.help` package (`CommandInfo`, `HelpFormat`, `SyntaxHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`); `CommandProcessor` and `ApplicationInitializer` updated.
