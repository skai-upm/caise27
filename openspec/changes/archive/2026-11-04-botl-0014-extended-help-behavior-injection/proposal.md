# Proposal

## Why

`HELP` only prints a syntax list kept as a constant inside `CommandProcessor`; users get no explanation of what commands do, and the help text is hard-wired into command processing. Delivery 3 adds an extended help with short descriptions and makes help generation receive its output behaviour by dependency injection so new help formats can be added later without touching command processing.

## What Changes

- `HELP` keeps its concise syntax list (same format as today).
- New `HELP EXTENDED` showing, for every registered command, its syntax and a brief explanation of its behaviour.
- Commands are registered once in a `CommandCatalog` of `CommandInfo` (syntax + description); both help modes use it.
- Help text is produced by a `HelpGenerator` that receives the catalog and a `HelpFormatter` strategy by constructor injection; `SyntaxHelpFormatter` and `ExtendedHelpFormatter` are the two current strategies. The composition root (`ApplicationContext`) creates and injects them into `CommandProcessor`.
- `HELP EXTENDED` itself is listed in both help outputs.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `application-control`: adds the `HELP EXTENDED` command and the requirement that help output behaviour is injected into the help component and decoupled from command processing (existing `HELP` and `EXIT` requirements unchanged).

## Impact

- **Code**: new presentation classes `CommandInfo`, `CommandCatalog`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`; `CommandProcessor` (dispatch `HELP`/`HELP EXTENDED` to injected generators; `COMMAND_SYNTAX` replaced by the catalog); `ApplicationContext` wiring; `App` obtains the processor from the composition root.
- **Behaviour**: unchanged for every existing command; `HELP` output gains the line `HELP EXTENDED`.
- **Mandatory milestone message**: `[F3-C04] Add extended help generation`.
- **Assumptions recorded**: description texts and the extended layout (syntax line followed by an indented description) are ours; `HELP EXTENDED;x` is rejected like `HELP;x`.
