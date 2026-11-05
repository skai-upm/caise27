# Tasks

## 1. Help strategies

- [x] 1.1 Add `presentation.help` (`CommandInfo`, `HelpFormat`, `SyntaxHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`); verify unit tests for both formats and a custom format

## 2. Wiring

- [x] 2.1 Add purposes to every command registration, expose the catalogue, register `HELP EXTENDED`, inject generators from `ApplicationInitializer`; verify HELP output unchanged (existing tests) and HELP EXTENDED shows syntax + purpose for every command; run `mvn clean package` and the JAR
