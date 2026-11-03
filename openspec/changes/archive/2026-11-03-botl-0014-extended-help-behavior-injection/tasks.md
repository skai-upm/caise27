# Tasks

## 1. Help components

- [x] 1.1 Create `CommandInfo`, `CommandCatalog` (all 22 commands with descriptions), `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator` (constructor-injected formatter); verify `HelpTest` for both formatters and an alternative formatter

## 2. Wiring

- [x] 2.1 Inject the HELP and HELP EXTENDED generators into `CommandProcessor` from `App`, derive `COMMAND_SYNTAX` from the catalog, and add `HELP EXTENDED` dispatch; verify `HELP` output is unchanged, `HELP EXTENDED` lists syntax + description for every command, `HELP EXTENDED;x` is rejected, and every catalog command is recognised by the processor

## 3. UML and verification

- [x] 3.1 Update `uml/model.puml` with the help classes; verify `plantuml -checkonly`, `mvn clean test` and the regression script (only the HELP list gains `HELP EXTENDED`)
