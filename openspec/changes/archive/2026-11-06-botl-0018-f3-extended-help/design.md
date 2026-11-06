# Design

## Context

`CommandProcessor` holds a static `HELP_TEXT` (Delivery 2 syntax plus INVOICE) and a `HELP` handler that returns it. Controllers are injected through `CommandProcessor`'s constructor by `AppConfiguration`.

## Goals / Non-Goals

**Goals:** one source of truth for command help, a Strategy for formatting injected into the generator, and HELP output unchanged.
**Non-Goals:** localisation, dynamic discovery of commands from handlers.

## Decisions

- **`CommandDescription(syntax, purpose)`** record and **`CommandCatalog.standard()`**, which returns the ordered list (the mandated HELP order, then the INVOICE commands, HELP EXTENDED, HELP, EXIT).
- **Strategy `HelpFormatter`** (`String format(List<CommandDescription>)`): `SyntaxHelpFormatter` prints `Available commands:` and one syntax per line. `ExtendedHelpFormatter` prints `Available commands (extended):` and, for each command, its syntax line followed by an indented purpose line. New formats only need a new `HelpFormatter`.
- **`HelpGenerator(List<CommandDescription>, HelpFormatter)`** gets both by constructor injection, and `generate()` delegates to the formatter. It knows nothing about command execution.
- **`CommandProcessor`** gets a `HelpGenerator help` and a `HelpGenerator extendedHelp` injected (extra constructor parameters), and the `HELP` / `HELP EXTENDED` handlers call `generate()`. The no-argument constructor builds the standard pair. `AppConfiguration` creates and injects them.
- `HELP_TEXT` is removed. Usage strings in handlers stay local.

## Risks / Trade-offs

- [The catalog and the handlers could drift] → A test checks that every catalog entry's keyword has a handler and vice versa.
