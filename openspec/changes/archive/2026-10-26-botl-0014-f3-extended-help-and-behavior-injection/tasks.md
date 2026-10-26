# Tasks

## 1. Help components

- [x] 1.1 Create `CommandInfo`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator`; verify `HelpGeneratorTest` (both formats over the same commands, a custom formatter injected, `CommandInfo.operation/parameterCount`)

## 2. Registry and processor

- [x] 2.1 Replace `COMMAND_SYNTAX` with the `COMMANDS` registry (syntax + description for every command, plus `HELP EXTENDED`), use it for parameter validation, and delegate HELP / HELP EXTENDED to injected generators; build them in `App.configure`; verify `CommandProcessorTest` (HELP output unchanged apart from the new entry, HELP EXTENDED shows descriptions, same commands in both, every description non-empty) and the whole suite

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` with the help package and its associations; verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.2 Run `mvn package`, the regression script (only the new HELP line may differ) and `HELP EXTENDED` through the JAR; verify outputs
