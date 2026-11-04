# Tasks

## 1. Help components

- [x] 1.1 Create `presentation.help` (`CommandInfo`, `CommandCatalog.standard()` with all current syntaxes plus `HELP EXTENDED` and descriptions, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator` with injected catalog and formatters); verify with `HelpGeneratorTest` (concise output identical to the previous HELP text plus the new line, extended output has syntax + description per command, a stub formatter is used when injected)

## 2. Wiring

- [x] 2.1 Inject `HelpGenerator` into `CommandProcessor` (remove `HELP_LINES`), dispatch `HELP` and `HELP EXTENDED`, wire in `App`, update tests; add `CommandCatalogTest` comparing catalog operations with the processor's `case` labels; verify `mvn clean test` passes

## 3. Model and integration

- [x] 3.1 Update `uml/model.puml` with the help classes, interface realisations and associations; verify PlantUML `-syntax`, `UmlModelTest` and the label/field check
- [x] 3.2 Run `mvn clean package` and the JAR with `HELP` and `HELP EXTENDED`
