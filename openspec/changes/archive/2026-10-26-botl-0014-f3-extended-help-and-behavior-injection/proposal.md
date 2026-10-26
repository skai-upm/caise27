# Proposal

## Why

`HELP` only prints command syntax from a hard-coded list inside the command processor. Operators need an extended help that also explains what each command does, and help generation must be decoupled so further presentation formats can be added without touching command processing.

## What Changes

- `HELP` keeps printing the concise syntax of every command.
- New `HELP EXTENDED` command printing each command's syntax and a brief description of its behaviour.
- Commands are registered once in a command catalog (syntax + description) that both help modes read.
- The class that builds help text receives the presentation behaviour (a help format strategy) through dependency injection; the command processor receives the configured help generators and does not create them.
- `uml/model.puml` updated with the help classes.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `command-interface`: adds `HELP EXTENDED`, the command catalog as single source for help, and injected help formats.
- `uml-model`: the model must show the help strategy structure.

## Impact

- New package `es.upm.parque.presentation.help` (`CommandDescriptor`, `CommandCatalog`, `HelpFormat`, `ConciseHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`); `CommandProcessor` and `App` updated; tests updated.
- No business or persistence change.
