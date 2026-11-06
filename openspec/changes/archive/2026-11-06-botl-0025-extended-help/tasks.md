# Tasks

## 1. Help generation

- [x] 1.1 Add `CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator` and verify with unit tests that the syntax formatter reproduces the previous `HELP` text plus `HELP EXTENDED`, the extended formatter shows a purpose for every command, and a custom formatter can be injected

## 2. Commands

- [x] 2.1 Inject the generators into `CommandProcessor`, add `HELP EXTENDED` (0 fields), remove the hard-coded help string, and verify with `CommandProcessor`/`MainTest` tests (existing HELP assertions still pass)
- [x] 2.2 Add `HELP EXTENDED` to the README command list and verify it matches `HELP`

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with `HELP` and `HELP EXTENDED` and verify the outputs
- [x] 3.2 Run `openspec validate botl-0025-extended-help --strict` and verify the change is valid
