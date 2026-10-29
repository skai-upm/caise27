# Proposal

## Why

Milestone F3-C01 (Delivery 3) requires presentation, business and persistence responsibilities to live in distinct Java packages, preserving the Delivery 2 behavior, and requires exceptions to represent and propagate abnormal situations, handled in a controlled way at the appropriate level so that an incorrect command never stops command processing. Today every class is in the single package `es.upm.amusementpark` and business rule violations are signalled with generic `IllegalArgumentException`s.

## What Changes

- Reorganize the code into packages under the root package `es.upm.amusementpark`:
  - `presentation` — command processing and user interaction (`CommandProcessor`);
  - `business.model` — domain model (`User`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`);
  - `business.control` — application control (`ParkController`);
  - `business.exception` — the business exception hierarchy;
  - `persistence` — repositories, park storage and data-directory handling (including creation and deletion of the `--fly` directory).
  - `App` stays in the root package as the composition root that wires the layers (the JAR entry point is unchanged).
- Introduce a business exception hierarchy rooted at `ParkException` with specific subclasses (element not found, duplicate element, invalid value, operation not allowed) and a `PersistenceException` in the persistence layer; business operations throw them, and the presentation layer catches them per command and reports an error, continuing with the next command. Presentation-level syntax errors use their own exception.
- Dependency rules: presentation depends on business only; persistence depends on business only; business depends on neither; domain objects contain no persistence logic.
- No externally observable behavior changes.

## Capabilities

### New Capabilities
- `architecture`: layered package organization, dependency rules between layers, and exception-based handling of abnormal situations.

### Modified Capabilities
- None (command behavior and messages are preserved).

## Impact

- All production classes move packages (Git history preserved with `git mv`); visibility of a few members changes from package-private to public where they are now used across packages (`reset`, reconstruction methods, `releaseGroup`).
- Tests are updated to the new packages and exception types; `uml/model.puml` is updated to show the Java packages.

## Ambiguities (flagged)

- The source lets students choose package names and between a centralized or distributed exception design; a centralized business hierarchy plus a persistence exception is chosen.
- Unchecked exceptions are used so that business signatures stay simple; handling is still explicit at the presentation level.
