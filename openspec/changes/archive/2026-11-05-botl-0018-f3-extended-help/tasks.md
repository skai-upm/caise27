# Tasks

## 1. Help components

- [x] 1.1 Create `presentation.help` (`CommandDescription`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`, `HelpCommands`); verify unit tests for identical plain output, extended output with purposes, and a custom injected formatter

## 2. Wiring

- [x] 2.1 Remove help from `CommandProcessor`, register help in `App.registerCommands`, and register help explicitly in tests that use `HELP`; verify all existing help assertions pass

## 3. Documentation and verification

- [x] 3.1 Update `uml/model.puml`; run `mvn -q package` and the JAR with `HELP` and `HELP EXTENDED`; verify all tests pass
