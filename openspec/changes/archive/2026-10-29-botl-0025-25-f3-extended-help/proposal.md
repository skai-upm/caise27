# Proposal

## Why

Milestone F3-C04 adds `HELP EXTENDED`, which shows a brief explanation of each command next to its syntax, and requires help generation to receive the behavior that builds its output through dependency injection, so new help variants can be added without touching command reading/execution.

## What Changes

- Add `HELP EXTENDED`: for every recognized command, its syntax and a one-line explanation of its purpose.
- `HELP` keeps printing the syntax list (now also listing `HELP EXTENDED`).
- Introduce a command catalog (syntax + description for every command), a `HelpFormat` strategy (basic: syntax only; extended: syntax + description) and a `HelpGenerator` that receives the format by constructor injection. `App` wires one generator per help variant into `CommandProcessor`, which only maps the command to the injected generator.
- Commit with the mandatory message `[F3-C04] Add extended help generation`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-control`: new `HELP EXTENDED` command and injected help generation.

## Impact

- New presentation classes `CommandDescriptor`, `CommandCatalog`, `HelpFormat`, `BasicHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`; `CommandProcessor` receives help generators; `App` wires them.
- Ambiguity flagged: explanation texts are the student's choice; one short sentence per command.
