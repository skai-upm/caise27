# Tasks

## 1. Help generation

- [x] 1.1 Create `CommandInfo`, `CommandCatalog` (syntax + description for every command, `HELP EXTENDED` after `HELP`), `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`; verify with unit tests that the syntax formatter output equals the previous HELP text plus `HELP EXTENDED`, every catalog entry has a description, and a custom formatter is used when injected

## 2. Commands and wiring

- [x] 2.1 Inject the two generators into `CommandProcessor` (constructor; defaults in the convenience constructor), dispatch `HELP EXTENDED`, and wire them in `App`; verify with processor tests for HELP, HELP EXTENDED and `HELP EXTENDED;x`
- [x] 2.2 Update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run of `HELP EXTENDED`
