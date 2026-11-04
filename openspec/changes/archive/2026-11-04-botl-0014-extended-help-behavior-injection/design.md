# Design

## Context

`CommandProcessor` (presentation) holds `public static final List<String> HELP_LINES` and builds the `HELP` output itself: `OK: Available commands:` followed by `"  " + syntax` per line. `AppTest` reads `HELP_LINES`. Since the previous change, `App` creates collaborators at startup and injects them.

## Goals / Non-Goals

**Goals:**
- One registry of supported commands feeding both help modes.
- Strategy-based formatting injected into the help builder.
- `HELP` output byte-identical to today, except for the new `HELP EXTENDED` line.

**Non-Goals:**
- New help formats beyond the two required modes.
- Changing how other commands are parsed or executed.

## Decisions

- **Package `presentation.help`.** Help is a presentation concern:
  - `CommandInfo`: a record (`operation`, `syntax`, `description`).
  - `CommandCatalog`: an ordered, immutable list of `CommandInfo`, with `commands()`, `operations()` and the factory `CommandCatalog.standard()` that defines today's 21 entries plus `HELP EXTENDED`. The syntax strings are copied unchanged from `HELP_LINES`, including the `(Campo: …)` note.
  - `HelpFormatter`: the strategy interface, `String format(List<CommandInfo>)`.
  - `SyntaxHelpFormatter` produces `OK: Available commands:` and `  <syntax>` lines, exactly the current text.
  - `ExtendedHelpFormatter` produces `OK: Available commands (extended):`, then per command `  <syntax>` and `      <description>`.
  - `HelpGenerator(CommandCatalog catalog, HelpFormatter conciseFormatter, HelpFormatter extendedFormatter)` offers `concise()` and `extended()`. It never instantiates a formatter or catalog.
- **Injection chain.** `App` creates `new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter(), new ExtendedHelpFormatter())` and passes it to `new CommandProcessor(controller, helpGenerator)`. `CommandProcessor` only calls `helpGenerator.concise()`/`extended()` after checking that there are no parameters.
- **Dispatch.** `HELP EXTENDED` is a two-word operation, consistent with `USER CREATE`, so the existing normalisation (upper case, single spaces) handles it, and `help extended` also works.
- **Keeping descriptions in sync.** `CommandCatalogTest` extracts the `case "…"` operation labels from `CommandProcessor.java` and asserts that the set equals `CommandCatalog.standard().operations()`. Adding a command without a catalog entry, or the reverse, fails the build. Descriptions are checked to be non-blank.
- **Order.** `HELP EXTENDED` is placed right after `HELP` in the catalog. All other entries keep their current order.
- **UML.**
  - New classes in a `presentation.help` package.
  - `CommandProcessor x--> HelpGenerator : helpGenerator`.
  - `HelpGenerator x--> CommandCatalog : catalog`.
  - `HelpGenerator x--> HelpFormatter : conciseFormatter` and `: extendedFormatter`.
  - `CommandCatalog *--> CommandInfo : commands`.
  - Realisations `HelpFormatter <|.. SyntaxHelpFormatter` and `HelpFormatter <|.. ExtendedHelpFormatter`.

## Risks / Trade-offs

- [Sync test reads source text] → simple and dependency-free. It fails loudly if the dispatch style changes, which is acceptable.
