# Proposal

## Why

All 22 production types live in one package, `es.upm.park`. `CommandProcessor` mixes command parsing and reply formatting with business rules: date, price, user-deletion and group-lock checks. `App` wires file storages directly. Delivery 3 requires a physical separation into presentation, business and persistence layers, without changing any observable behaviour.

## What Changes

- New package structure:
  - `es.upm.park.presentation`: `App`, `CommandRunner`, `CommandProcessor` (command interaction, parsing and reply formatting only).
  - `es.upm.park.business.model`: `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`.
  - `es.upm.park.business.service`: `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `UserValidator`, plus two new classes. `ParkController` is the main controller; it owns the system date and price and applies the cross-cutting rules now inside `CommandProcessor`. `ParkSession` opens permanent or `--fly` data and cleans up.
  - `es.upm.park.persistence`: all `*FileStorage` classes, `DataFiles` (moved out of `App`), `TemporaryDataDirectory`.
- Allowed dependencies: presentation → business; business.service → business.model and persistence; persistence → business.model. The model depends on nothing else, and presentation never uses persistence.
- No class remains in `es.upm.park` itself; the default package is not used.
- Command syntax, replies, file formats, `--fly` and every E1/E2 rule are unchanged.
- `uml/model.puml` is regenerated with the packages; tests move into the matching packages, and a layering test is added.

## Capabilities

### New Capabilities
- `application-layering`: Package structure, dependency rules and behaviour preservation of the layered architecture.

### Modified Capabilities
- `system-date`: "Single date source" — the date owner is now named as the business-layer park controller (behaviour unchanged).

## Impact

- Every production and test source file moves package. Some package-private members become public because they are used across packages: `User.copyEditableFrom`, `Group.add/remove/copy`, the history constructors of `IndividualTicket`/`GroupTicket`.
- New: `ParkController`, `ParkSession`, `LayeringTest`. `UmlModelTest` is made package-aware.
- No new dependencies. The JAR main class becomes `es.upm.park.presentation.App` (manifest updated).
