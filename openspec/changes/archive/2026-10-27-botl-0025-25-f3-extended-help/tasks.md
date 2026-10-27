# Tasks

## 1. Help generation

- [x] 1.1 Create `presentation.help` (`CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`); verify with `HelpGeneratorTest` the syntax format equals the previous HELP text plus `HELP EXTENDED`, every command has a description in the extended format, and a custom injected formatter is used

## 2. Command integration

- [x] 2.1 Inject the help generators into `CommandProcessor`, add `HELP EXTENDED`, update `uml/model.puml`; verify with `CommandProcessorTest` (HELP, HELP EXTENDED, catalogue commands recognised), `UmlModelTest`, PlantUML check and `mvn package`
