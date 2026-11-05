# Design

## Context

Current packages are `es.upm.amusementpark` (`App`), `.cli` (processor), `.park` (domain plus command handlers) and `.persistence`. Each handler catches `ParkException` and prints. See proposal.md - Why.

## Decisions

- **Package mapping**: `cli` and the `*Commands` classes become `presentation`; the rest of `park` becomes `business`. Moves use `git mv` so history is kept, followed by mechanical `package`/`import` rewrites. Tests move with their subjects. Tests that need package-private business access stay in `business`.
- **Exceptions**:
  - `presentation.InvalidCommandException` (unchecked) is thrown for syntax, date, price and capacity format errors. Its message is the existing text without the `ERROR: ` prefix.
  - `business.ParkException` stays as the business-rule exception.
  - `CommandProcessor.dispatch` catches `InvalidCommandException` and `ParkException` and prints `ERROR: <message>`. Any other `RuntimeException` gets `ERROR: Could not process command: <line>`.
  - Handlers no longer contain try/catch blocks or print errors, so exceptions are part of the design rather than a print replacement.
- **Composition root**: `App` (root package) is the only class that knows all three layers. Persistence depends on business (restore APIs). Presentation depends on business.
- **`LayeringTest`** uses textual import checks with plain String operations. It forbids `java.io.File`, `java.nio.file` and the persistence package in presentation and business.

## Risks / Trade-offs

- [Large mechanical diff] → behavior is guarded by the existing 134 tests, which must stay green with unchanged message assertions.
