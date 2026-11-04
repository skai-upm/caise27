# Proposal

## Why

`HELP` only lists command syntax, so users cannot learn what each command does, and the help text is hard-coded inside the command processor. An extended help with short explanations is needed, and help construction must receive its formatting behavior by injection so new presentation formats can be added without touching command processing.

## What Changes

- `HELP` keeps the concise syntax list (now also listing the new `HELP EXTENDED`).
- New `HELP EXTENDED` shows each available command's syntax followed by a brief explanation of its behavior.
- Command information (syntax and description) is kept in one catalog of registered commands used by both modes.
- A help generator class builds the help text from the catalog using a formatting strategy injected through its constructor; concise and extended formats are two strategies. The command processor receives the configured help generators instead of building help itself; wiring happens at application startup.
- No change to the execution of any other command.

The source suggests change ID `extended-help-with-injected-renderer`; this batch uses the BOTL ID `botl-0014-14-f3-extended-help-and-behavior-injection`. Mandatory milestone message: `[F3-C04] Add extended help generation`. The brief suggests a capability `command-help`; the existing `application-control` capability already owns `HELP`, so it is extended instead of creating a duplicate.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `application-control`: `HELP` also lists `HELP EXTENDED`; new requirements for `HELP EXTENDED` and for injected help formatting.

## Impact

- Presentation: new `CommandInfo`, `CommandCatalog`, `HelpFormatter` (interface), `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`; `CommandProcessor` takes help generators; `App` wires them.
- `uml/model.puml` updated. No business or persistence changes.
