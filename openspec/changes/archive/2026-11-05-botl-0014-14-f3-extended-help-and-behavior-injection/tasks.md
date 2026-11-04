# Tasks

## 1. Help components

- [x] 1.1 Implement `CommandInfo`, `CommandCatalog.standard()` (all commands with descriptions, current HELP order plus `HELP EXTENDED`), `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator` (constructor injection); verify with `HelpGeneratorTest` (concise output identical to the previous HELP lines plus `HELP EXTENDED`, extended output has a description for every command, custom formatter used when injected)

## 2. Command processing

- [x] 2.1 Inject the two help generators into `CommandProcessor`, add `HELP EXTENDED` parsing (no parameters), wire them in `App.createProcessor`; verify with `CommandProcessorTest` (HELP, HELP EXTENDED, HELP EXTENDED;x, alternative formatter through the processor) and `mvn test`

## 3. Regression and documentation

- [x] 3.1 Diff the E1/E2 script output with the previous commit's JAR (only the added `HELP EXTENDED` line may differ), run the layer check, update `uml/model.puml` and verify PlantUML `-checkonly` and the code cross-check
