# Tasks

## 1. Help components

- [x] 1.1 Create `CommandDescriptor`, `CommandCatalog` (with `parkCommands()` including descriptions for all commands and `HELP EXTENDED`), `HelpFormat`, `ConciseHelpFormat`, `ExtendedHelpFormat` and `HelpGenerator`; verify unit tests for both formats and for a custom format injected into the generator

## 2. Command processing

- [x] 2.1 Replace the hard-coded syntax list in `CommandProcessor` with injected help generators keyed by mode, handle `HELP` and `HELP EXTENDED` (unknown mode and parameters rejected), and wire the catalog and generators in `App.initialize`; verify command tests for both modes and that every command family is in the catalog, then run `mvn test`

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with the help package; verify `plantuml -checkonly` and the model/code cross-check pass
