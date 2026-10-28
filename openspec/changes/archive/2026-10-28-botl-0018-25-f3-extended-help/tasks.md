# Tasks

## 1. Help generation

- [x] 1.1 Add `CommandHelp`, `CommandCatalog` (all commands incl. `HELP EXTENDED` with descriptions), `HelpStrategy`, `SyntaxHelpStrategy`, `ExtendedHelpStrategy` and `HelpGenerator`; verify with unit tests (custom strategy injected, both built-in strategies)
- [x] 1.2 Make `HelpCommand` use an injected `HelpGenerator`, register `HELP` and `HELP EXTENDED` in `CommandProcessor`; verify with command tests (HELP unchanged + lists HELP EXTENDED, HELP EXTENDED shows every syntax with an explanation)

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with `HELP EXTENDED`; verify output
- [x] 2.2 Commit with the exact message `[F3-C04] Add extended help generation`; verify with `git log -1 --format=%s`
