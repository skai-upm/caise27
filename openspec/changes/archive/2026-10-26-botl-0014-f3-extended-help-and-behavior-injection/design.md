# Design

## Context

`CommandProcessor` holds a static `COMMAND_SYNTAX` array and builds the `HELP` text itself. Controllers are singletons configured in `App.initialize`, which also creates the `CommandProcessor`.

## Goals / Non-Goals

**Goals:** a single registry of commands; help formats as injected behaviour (Strategy); help modes pluggable without touching command parsing.

**Non-Goals:** localisation, per-command help (`HELP USER`), changes to other commands.

## Decisions

- **Package `es.upm.parque.presentation.help`** (help is presentation behaviour):
  - `CommandDescriptor` — immutable `syntax` + `description`.
  - `CommandCatalog` — ordered list of registered descriptors (`register(syntax, description)`, `getCommands()`); `CommandCatalog.parkCommands()` registers every park command in one place.
  - `HelpFormat` — strategy interface `String format(List<CommandDescriptor>)`.
  - `ConciseHelpFormat` (syntax only, same layout as today) and `ExtendedHelpFormat` (syntax plus indented description).
  - `HelpGenerator` — receives `CommandCatalog` and `HelpFormat` in its constructor; `generate()` delegates to the format. It never creates its dependencies.
- **Pluggable modes**: `CommandProcessor` receives a `Map<String, HelpGenerator>` keyed by mode (`""` for `HELP`, `"EXTENDED"` for `HELP EXTENDED`). `HELP <MODE>` looks the mode up; unknown modes are errors. Adding a format later only means registering another entry in `App.initialize`. Alternative: two fixed fields — rejected because every new format would change the processor.
- **`App.initialize`** creates the catalog once and two generators sharing it.

## Risks / Trade-offs

- [Catalog and implemented commands can diverge] → a test checks that every operation keyword handled by the processor appears in the catalog.
