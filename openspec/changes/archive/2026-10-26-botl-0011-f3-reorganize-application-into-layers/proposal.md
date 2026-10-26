# Proposal

## Why

All 20 classes live in one package and `CommandProcessor` mixes command parsing, business orchestration (system date, price, cross-entity rules) and object wiring. Delivery 3 requires a layered structure that physically separates presentation, business and persistence so each responsibility can evolve independently.

## What Changes

- Packages:
  - `es.upm.parque.presentation`: `App` (startup, argument parsing, input sources) and `CommandProcessor` (command parsing with `String.split`, response formatting, HELP, EXIT).
  - `es.upm.parque.business.model`: domain model and value rules — `User`, `UserField`, `Group`, `Admission`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `UserValidator`.
  - `es.upm.parque.business.controller`: `ParkController` (new; owns system date and ticket price and the cross-entity rules previously embedded in `CommandProcessor`) and the managers `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`.
  - `es.upm.parque.persistence`: repositories/file access `UserStorage`, `GroupStorage`, `TicketStorage`, `AttractionStorage` and `DataDirectory`.
- The default package is not used; `CommandProcessor` never touches storages or files; domain objects never read or write files.
- No change in command syntax, responses or file formats; all E1/E2 commands keep working; `mvn package` still builds.
- Tests move to the matching packages; `uml/model.puml` shows the packages.

## Capabilities

### New Capabilities
- `application-architecture`: The mandatory layered package structure and the dependency rules between layers.

### Modified Capabilities

## Impact

- Every source and test file moves package; some constructors/helpers used across packages become public (ticket restoration constructors, `GroupTicket.componentId`).
- `CommandProcessor` delegates to `ParkController`; its constructor now takes the controller. `App` builds the controller from a `DataDirectory` via `ParkController.load`.
- Required commit title for the delivery: `[F3-C01] Reorganize application into layered packages`.
