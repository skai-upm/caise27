# Tasks

## 1. Help generation

- [x] 1.1 Add `CommandDescription`, `CommandCatalog.standard()` (all current commands with purposes, plus HELP EXTENDED), the `HelpFormatter` strategy with `SyntaxHelpFormatter`/`ExtendedHelpFormatter`, and `HelpGenerator`, and verify with `HelpGeneratorTest` the output of both formatters and of a custom formatter

## 2. Integration

- [x] 2.1 Inject the two help generators into `CommandProcessor` (handlers `HELP` and `HELP EXTENDED`), remove `HELP_TEXT`, wire them in `AppConfiguration`, and verify that the existing HELP tests pass unchanged and that a new test covers HELP EXTENDED
- [x] 2.2 Add a test that every catalog keyword has a handler and every handler is in the catalog, and verify that it passes
- [x] 2.3 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
