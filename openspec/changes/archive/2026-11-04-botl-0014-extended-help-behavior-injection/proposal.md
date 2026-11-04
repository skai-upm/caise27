# Proposal

## Why

`HELP` only lists syntax, which is built from a hard-coded `USAGE` list inside `CommandProcessor`. Users need a help mode that also explains what each command does. The help construction must be decoupled so future presentation formats can be plugged in without touching command parsing.

## What Changes

- `HELP` keeps its concise syntax listing and now also lists `HELP EXTENDED`.
- New command `HELP EXTENDED` prints every registered command's syntax with a brief description of its behaviour.
- New presentation components:
  - `CommandInfo` (syntax + description) and `CommandCatalog`, the single registry of the application's commands, used by both help modes;
  - `HelpFormatter`, a strategy interface, with `SyntaxHelpFormatter` (concise) and `ExtendedHelpFormatter`;
  - `HelpGenerator`, which receives the commands and a formatter through its constructor.
- `CommandProcessor` receives its two `HelpGenerator`s through its constructor; `App` wires them at startup. The hard-coded `USAGE` list is removed.
- Command semantics are unchanged.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `application-control`: "HELP command" is based on the registered commands and lists `HELP EXTENDED`. Added "HELP EXTENDED command" and "Injected help generation behaviour".

## Impact

- New: `CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator` (presentation) and tests.
- Changed: `CommandProcessor` (constructor, `HELP EXTENDED`), `App` (wiring), tests that build a `CommandProcessor`, `uml/model.puml`.
