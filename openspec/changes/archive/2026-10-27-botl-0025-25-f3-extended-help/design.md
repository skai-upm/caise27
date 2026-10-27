# Design

## Context

`CommandProcessor.HELP_TEXT` is a hard-coded string. Delivery 3 also asks for extended help and for the help-building behaviour to be injected.

## Goals / Non-Goals

**Goals:**
- One catalogue of commands; pluggable formatting (Strategy) injected into the generator; HELP output unchanged except the new `HELP EXTENDED` entry.

**Non-Goals:**
- Pass commands (added to the catalogue when implemented in F3-C06).

## Decisions

- **`CommandInfo`** record `(syntax, description)`; **`CommandCatalog.commands()`** returns the ordered list of all commands (same order as the current HELP).
- **Strategy:** `HelpFormatter` interface `List<String> format(List<CommandInfo>)`; `SyntaxHelpFormatter` produces `Available commands:` + `  <syntax>` lines (identical to the current HELP); `ExtendedHelpFormatter` produces `Available commands (extended):` + `  <syntax>` + `      <description>`.
- **`HelpGenerator`** receives a `HelpFormatter` (constructor injection) and the catalogue; `generate()` returns the text.
- **Wiring:** `CommandProcessor(PrintStream out, HelpGenerator help, HelpGenerator extendedHelp)`; the one-argument constructor wires the defaults (syntax / extended). Command reading/execution do not know the formats.
- **Dispatch:** `HELP EXTENDED` is a distinct command name (element 0 of the `;` split).
- **Mandatory commit:** `[F3-C04] Add extended help generation` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Catalogue and dispatcher could drift] → A test checks that each catalogue syntax's command name is accepted by the dispatcher (no "unrecognised command" error).
