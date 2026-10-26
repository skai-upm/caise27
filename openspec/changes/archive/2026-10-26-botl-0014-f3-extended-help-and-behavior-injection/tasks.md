# Tasks

## 1. Help components

- [x] 1.1 Create `CommandInfo`, `CommandCatalog` (standard list with descriptions, `syntaxOf`), `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator`; verify with unit tests (concise output, extended output contains every command with description, unknown mode, custom formatter injected)

## 2. Integration

- [x] 2.1 Inject `HelpGenerator` into `CommandProcessor`, route `HELP` / `HELP <mode>`, replace syntax constants by catalog lookups, wire in `App`; verify `mvn test` passes and the regression output is unchanged except for the new `HELP EXTENDED` line in HELP
- [x] 2.2 Update `uml/model.puml` with the help classes; verify with `plantuml -checkonly`
