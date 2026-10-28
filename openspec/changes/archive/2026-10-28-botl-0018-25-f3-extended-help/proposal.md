# Proposal

## Why

Milestone F3-C04 keeps `HELP` (syntax of every recognised command) and adds `HELP EXTENDED`, which shows a brief explanation of each command's purpose next to its syntax. The class that generates help text must receive the behaviour used to build its output through dependency injection, so new help styles can be added later without coupling them to command reading and execution.

## What Changes

- Introduce a command help catalogue (syntax + short purpose for every command).
- Introduce a help generator that receives a help-formatting strategy by injection; provide a syntax-only strategy (used by `HELP`) and an extended strategy (used by `HELP EXTENDED`).
- Add the `HELP EXTENDED` command; `HELP` keeps its output and also lists `HELP EXTENDED`.
- The command processor wires the generators with their strategies at initialization; command reading/dispatch is unchanged.
- Commit with the exact message `[F3-C04] Add extended help generation`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `execution-control`: HELP also lists `HELP EXTENDED`; new `HELP EXTENDED` requirement; help generation uses an injected strategy.

## Impact

- New presentation classes (`CommandCatalog`, `CommandHelp`, `HelpGenerator`, `HelpStrategy` + two strategies); `HelpCommand` receives a generator; tests.
- **Flagged ambiguities:** the wording of the explanations and the extended layout are not specified; each entry shows the syntax line followed by an indented one-line explanation.
