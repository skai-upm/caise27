# Design

## Context

`CommandProcessor` holds a constant `HELP_TEXT`. See proposal.md.

## Goals / Non-Goals

**Goals:** strategy-based help formats, injected; single source of command syntax/descriptions.
**Non-Goals:** localization.

## Decisions

- **`CommandDescriptor(syntax, description)`** and **`CommandCatalog.all()`**: ordered descriptors for every command (including `HELP EXTENDED`).
- **`HelpFormat`** (functional interface, `String format(List<CommandDescriptor>)`): `BasicHelpFormat` prints `Available commands:` + syntax lines + the `<Field>` hint; `ExtendedHelpFormat` prints `<syntax>` then `    <description>` per command.
- **`HelpGenerator(List<CommandDescriptor> catalog, HelpFormat format)`**: constructor injection; `generate()` returns the text.
- **`CommandProcessor(ParkController, HelpGenerator help, HelpGenerator extendedHelp)`**: `HELP` / `HELP EXTENDED` delegate to the injected generators. A convenience constructor `CommandProcessor(ParkController)` wires the default ones (used by tests); `App` wires them explicitly.

## Risks / Trade-offs

- [Catalog must list new commands] → single place; tests check HELP and HELP EXTENDED cover the same syntaxes.
