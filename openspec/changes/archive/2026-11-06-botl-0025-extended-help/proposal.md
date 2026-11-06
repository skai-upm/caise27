# Proposal

## Why

Milestone F3-C04 (Delivery 3, step 4) adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose alongside its syntax, and requires the help generator to receive the output-building behavior through dependency injection so new help formats can be added without touching command reading and execution.

## What Changes

- New command `HELP EXTENDED`: lists every available command with its syntax and a brief purpose.
- `HELP` keeps listing the syntax of every recognized command (now including `HELP EXTENDED`).
- Help text is produced by a help generator that is given, by injection, the formatting behavior (syntax-only or extended) and the catalog of commands; the command processor is given both generators at construction and does not know how help is formatted.

### In scope
- Source section 7.

### Out of scope
- Other help formats (the design only prepares for them).

## Capabilities

### New Capabilities
- `extended-help`: the `HELP EXTENDED` command and the injectable help-generation behavior.

### Modified Capabilities
- `execution-control`: `HELP` also lists `HELP EXTENDED`.

## Impact

- New `es.upm.park.presentation.help` package (`CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`); `CommandProcessor` receives the generators; README.
