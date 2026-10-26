# Tasks

## 1. Command catalog and help formatting

- [x] 1.1 Create `CommandInfo` and `CommandCatalog` (all commands incl. HELP EXTENDED, syntax + description) and use its syntaxes for parameter-error usages in `CommandProcessor`; verify with `CommandCatalogTest` (all syntaxes unique, descriptions non-empty)
- [x] 1.2 Create `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter` and `HelpGenerator` (formatter injected); verify with `HelpGeneratorTest` including a custom formatter

## 2. Commands and wiring

- [x] 2.1 Inject help generators into `CommandProcessor` (modes "" and "EXTENDED"), handle `HELP EXTENDED` and unknown modes, wire them in `App.configure`; verify with `CommandProcessorTest` (HELP, HELP EXTENDED, HELP SHORT, HELP;extra) and `mvn test`
- [x] 2.2 Update `uml/model.puml` with the help classes; verify `plantuml -checkonly` and the field cross-check pass
