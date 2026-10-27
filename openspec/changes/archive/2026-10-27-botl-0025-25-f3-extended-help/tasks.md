# Tasks

## 1. Help generation

- [x] 1.1 Create `presentation.help` (catalogue, formatter interface, syntax and extended formatters, generator with injected formatter); verify with `HelpGeneratorTest` (basic lists syntax only, extended adds purposes, a custom formatter is honoured)
- [x] 1.2 Inject the generators into `CommandProcessor` from `Main`, add `HELP EXTENDED`, remove the hard-coded help text; verify HELP-related tests in `CommandProcessorTest` and a manual `HELP EXTENDED` run

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F3-C04] Add extended help generation`; verify with `git log -1 --format=%s`
