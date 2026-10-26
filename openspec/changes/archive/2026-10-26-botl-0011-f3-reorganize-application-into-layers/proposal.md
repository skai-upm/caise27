# Proposal

## Why

All classes live in a single package (`es.upm.parque`), so presentation, business rules and file access are only separated by convention. Delivery 3 requires a physical layered structure — presentation, business and persistence in distinct packages — while preserving every Delivery 1 and 2 feature and command exactly as it behaves today.

## What Changes

- Move the code into layer packages (the default package is never used):
  - `es.upm.parque.presentation` — command-based interaction: `CommandProcessor` (command parsing, dispatch, responses) and `ExecutionOptions` (program arguments).
  - `es.upm.parque.business` — domain and rules, with subpackages `model` (`User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, value parsing/formatting), `controller` (the managers, renamed to controllers: `UserController`, `GroupController`, `TicketController`, `AttractionController`, plus `MainController`) and `validation` (`UserValidator`); `ParkException` at the layer root.
  - `es.upm.parque.persistence` — repositories, `TextFiles` and `DataDirectory`.
  - `es.upm.parque.App` remains the entry point that assembles the layers (the executable JAR's main class is unchanged).
- Date and price parsing/formatting move from `MainController` to a model helper so persistence no longer depends on a controller.
- Commands keep their syntax and behaviour; presentation only talks to controllers, never to repositories or files; domain objects never read or write files.
- Tests are moved to the matching packages; an architecture test checks the layer rules.
- `uml/model.puml` shows the packages.

## Capabilities

### New Capabilities
- `layered-architecture`: The physical layer structure of the code — separate presentation, business and persistence packages, no default package, and the allowed dependencies between layers.

### Modified Capabilities
<!-- None: no command or behaviour changes; existing specs keep applying unchanged. -->

## Impact

- Every source and test file moves package; managers are renamed to controllers. No change to commands, outputs or data file formats; `mvn package` still produces `target/parque.jar` with main class `es.upm.parque.App`.
