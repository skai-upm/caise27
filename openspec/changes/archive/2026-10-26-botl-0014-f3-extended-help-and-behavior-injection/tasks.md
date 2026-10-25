# Tasks

## 1. Help components

- [x] 1.1 Create `CommandInfo`, `CommandRegistry.standard()` (all commands with descriptions, incl. `HELP EXTENDED`), `HelpFormat`, `SyntaxHelpFormat`, `ExtendedHelpFormat` and `HelpGenerator` with constructor injection; verify with `HelpGeneratorTest` (both formats, stub format injected)

## 2. Wiring

- [x] 2.1 Replace `COMMAND_SYNTAX` in `CommandProcessor` with injected help modes (`Map<String, HelpGenerator>`), wire `HELP` and `HELP EXTENDED` in `Bootstrap`; verify with `CommandProcessorTest` (HELP unchanged, HELP EXTENDED, same commands/order, every registered command recognised, extra mode registered without processor change) and `SingletonTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` with the help classes, strategy realisations and associations; verify `plantuml -checkonly` and `UmlModelTest`
- [x] 3.2 Verify end to end with `mvn package`: `HELP` output identical to before and `HELP EXTENDED` showing descriptions
