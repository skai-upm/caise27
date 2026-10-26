# Proposal

## Why

`HELP` only lists command syntax, which is not enough to understand what each command does, and the help text is hard-wired inside the command processor. Delivery 3 adds an extended help and asks that the way help is presented be injected into the class that builds it, so new presentation formats can be added later without touching general command processing.

## What Changes

- `HELP` keeps its concise output: the syntax of every registered command.
- New command `HELP EXTENDED`: the syntax of every registered command together with a brief explanation of its behaviour.
- Commands are registered once, each with its syntax and a description; both help modes are generated from that registry.
- A help generator class builds the help text from the registered commands using a presentation behaviour (format strategy) that it receives through dependency injection; it does not instantiate its dependencies. Two behaviours exist: syntax-only and syntax-with-description. Help generators are created and configured at application startup and injected into the command processor, keyed by the help command they serve.
- No change to the other commands.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `command-interface`: HELP is generated from the command registry by an injected help behaviour, and a new `HELP EXTENDED` command shows syntax plus descriptions.

## Impact

- New `es.upm.parque.presentation.help` package (`CommandInfo`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`); `CommandProcessor` replaces its syntax list with a registry of `CommandInfo` and delegates HELP/HELP EXTENDED to injected generators; `App.configure` creates them.
- `uml/model.puml` updated. Tests for both modes, injection and registry coverage.
