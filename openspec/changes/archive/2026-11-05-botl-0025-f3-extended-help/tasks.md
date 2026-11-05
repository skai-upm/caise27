## 1. Help model and strategy

- [x] 1.1 Add `CommandDescriptor` and `CommandCatalog`: every command with its syntax and a brief purpose, in HELP order, including `HELP EXTENDED`.
- [x] 1.2 Add the `HelpFormatter` interface, with `SyntaxHelpFormatter` (current HELP text) and `ExtendedHelpFormatter` (syntax plus purpose).
- [x] 1.3 Add `HelpGenerator`, which receives the catalog and the formatter by constructor injection.

## 2. Wiring

- [x] 2.1 Add the `HelpCommands` registry (handles/respond, with an extra-field check) and `HelpConfiguration.standard()`, which registers `HELP` and `HELP EXTENDED`.
- [x] 2.2 Inject `HelpCommands` into `CommandProcessor`:
  - delegate registered help commands before the dispatch switch;
  - remove `HELP_SYNTAX` and `HELP_TEXT`;
  - `App` uses the injecting constructor.

## 3. Verification and documentation

- [x] 3.1 Update the tests that pin the HELP list to use `CommandCatalog`. Add help tests:
  - extended output with purposes, in order;
  - plain help without purposes;
  - an extra-field error;
  - a custom injected variant registered without changing `CommandProcessor`;
  - catalog completeness.
- [x] 3.2 Document `HELP EXTENDED` in README.
- [x] 3.3 Run `mvn package` and an end-to-end JAR regression: all output identical to the baseline except the added `HELP EXTENDED` line in HELP.
