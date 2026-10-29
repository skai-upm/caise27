# Design

## Context

`CommandProcessor` holds a static `COMMAND_SYNTAX` list and prints it in `printHelp()`.

## Goals / Non-Goals

**Goals:** HELP EXTENDED; Strategy-based, injected help generation. **Non-Goals:** changing HELP's existing text.

## Decisions

- **`CommandInfo`** (record: `syntax`, `description`) and **`CommandCatalog`** (`List<CommandInfo> commands()`, default catalog with all commands in the Delivery 2 order plus the INVOICE commands and `HELP EXTENDED` after `HELP`).
- **`HelpFormatter`** (Strategy interface): `String format(List<CommandInfo>)`. `SyntaxHelpFormatter` reproduces the current output (`Available commands:` + `  <syntax>` lines); `ExtendedHelpFormatter` prints `Available commands (extended):` and for each command `  <syntax>` then `      <description>`.
- **`HelpGenerator`**: constructor-injected `CommandCatalog` and `HelpFormatter`; `String generate()`.
- **`CommandProcessor`**: constructor receives `HelpGenerator help` and `HelpGenerator extendedHelp`; `HELP` and `HELP EXTENDED` (no arguments) print the respective generator's text. The convenience constructor wires defaults; `App` creates and injects both explicitly.

## Risks / Trade-offs

- [Two generators injected] → keeps each command bound to a configured behavior; future variants are new formatters.
