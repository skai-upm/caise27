# Tasks

## 1. Help generation

- [x] 1.1 Create `CommandDescriptor`, `CommandCatalog`, `HelpFormat`, `BasicHelpFormat`, `ExtendedHelpFormat`, `HelpGenerator`; verify unit tests: basic output equals the previous HELP list (plus `HELP EXTENDED`), extended output pairs every syntax with a description, and a custom injected format is used
- [x] 1.2 Inject help generators into `CommandProcessor` (wired by `App`) and add `HELP EXTENDED`; verify processor tests and `java -jar` with `HELP EXTENDED`

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F3-C04] Add extended help generation`; verify with `git log -1 --format=%s`
