# Tasks

## 1. Help components

- [x] 1.1 Add `CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator`; verify with `HelpGeneratorTest` (concise text identical to previous HELP plus `HELP EXTENDED`, extended descriptions, alternative formatter used)

## 2. Commands and wiring

- [x] 2.1 Inject the two generators into `CommandProcessor`, add `HELP EXTENDED`, remove `USAGE`, wire in `App`, update tests; verify with `CommandProcessorTest` and `CommandCatalogTest` (all catalog operations recognised)

## 3. Model and regression

- [x] 3.1 Update `uml/model.puml` (interface, realisations, injected associations); verify `UmlModelTest` and the PlantUML `-syntax` check
- [x] 3.2 Run `mvn clean package` and the E1/E2 regression script; outputs identical except the new `HELP EXTENDED` HELP line
