# Tasks

## 1. Help components

- [x] 1.1 Create `CommandInfo`, `CommandCatalog.standard()` (all commands with descriptions, `HELP EXTENDED` included), `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator`; verify `HelpGeneratorTest` (syntax format, extended format, alternative injected formatter used)

## 2. Integration

- [x] 2.1 Inject the two generators into `CommandProcessor` (dispatch `HELP` / `HELP EXTENDED`, remove `COMMAND_SYNTAX`), add `ApplicationContext.commandProcessor()` and use it from `App`; verify `HelpCommandsTest` (HELP unchanged lines + HELP EXTENDED line, extended descriptions, same order, `HELP EXTENDED;x` rejected, every catalog operation is recognised) and existing HELP tests

## 3. Documentation and regression

- [x] 3.1 Update `uml/model.puml` (strategy interface, realizations, generator injection) and verify `UmlModelTest`, `LayeringTest`, `plantuml -checkonly`, `mvn package`, and the F3 regression script identical to the baseline except for the added `HELP EXTENDED` line in HELP
