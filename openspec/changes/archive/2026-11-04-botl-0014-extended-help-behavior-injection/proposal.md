# Proposal

## Why

`HELP` only lists syntax, from a hard-coded `HELP_LINES` list inside `CommandProcessor`. Users need a short explanation of each command, and the way help is rendered should be replaceable (new formats later) without touching command processing.

## What Changes

- `HELP` keeps printing the concise syntax list exactly as today, plus the new `HELP EXTENDED` entry.
- New command `HELP EXTENDED` prints each command's syntax and a brief description.
- A command registry (`CommandCatalog` of `CommandInfo`: operation, syntax, description) becomes the single source for both help modes and replaces `HELP_LINES`.
- A `HelpGenerator` builds the help text. It receives the catalog and one `HelpFormatter` strategy per mode (`SyntaxHelpFormatter`, `ExtendedHelpFormatter`) through its constructor. `App` creates them and injects the generator into `CommandProcessor`.
- A test keeps the catalog and the processor's accepted operations in sync.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `application-control`: `HELP` also lists `HELP EXTENDED`. New requirements cover `HELP EXTENDED`, the command registry as the source of both modes, and injected formatting behaviour.

## Impact

- New package `presentation.help`. `CommandProcessor` gets a `HelpGenerator` constructor parameter and loses `HELP_LINES`. `App` wiring and the tests that build processors are updated.
- No change to other commands or to data files. UML updated.
- Delivery milestone message: `[F3-C04] Add extended help generation`.
