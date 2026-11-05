# Proposal

## Why

Milestone F3-C01 opens Delivery 3 by reorganizing the code into distinct presentation, business and persistence packages, with no default package, while keeping all Delivery 2 behavior. It also requires exceptions to represent abnormal situations and to be handled at the right level, so that an incorrect command or an operation that can't be completed never stops command processing. Today command classes sit in the `park` package next to the domain model, and each command catches and prints its own errors.

## What Changes

- Move packages without changing behavior:
  - `es.upm.amusementpark.presentation` takes command processing (`CommandProcessor`, `CommandHandler`) and all command handlers (`SystemCommands`, `GroupCommands`, `TicketCommands`, `AttractionCommands`).
  - `es.upm.amusementpark.business` takes the model, business rules and control (`ParkController`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `ParkException`, `UserDirectory`, `GroupTicketDates`).
  - `es.upm.amusementpark.persistence` keeps the repositories and `ParkStorage`.
  - `App` stays in the root package as the entry point and composition root.
- Use exceptions in the design. Handlers signal malformed input with a presentation-level `InvalidCommandException`, and business rules signal refusals with `ParkException`. Both propagate to `CommandProcessor`, which handles them in one place by printing `ERROR: <message>` and continuing. Unexpected runtime exceptions keep the existing generic message. Output text is unchanged.
- Enforce the layering rules with tests: every source file declares a package; presentation doesn't import persistence; business imports neither presentation nor persistence; domain objects don't read or write files.
- Update `uml/model.puml` to the new packages.

### Out of scope
- New commands or behavior changes (later Delivery 3 milestones).

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering of the application and the dependency rules between layers.

### Modified Capabilities
- `application-runtime`: controlled handling now explicitly covers rejected operations and malformed commands signaled by exceptions, not only unexpected failures.

## Impact

- Every production and test file moves package (imports updated).
- Command classes simplified.
- `CommandProcessor` gains centralized exception handling.
- `uml/model.puml` and new `LayeringTest`.
