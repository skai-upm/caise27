# Proposal

## Why

After Deliveries 1 and 2 all classes live in one package and `CommandProcessor` mixes command parsing with business state (system date, price) and cross-entity rules. Delivery 3 builds on a layered structure; presentation, business and persistence must be physically separated without changing any observable behaviour.

## What Changes

- New packages under `es.upm.etsisi.amusementpark`:
  - `presentation`: command interaction — `CommandProcessor` (parsing, responses, HELP/EXIT) and `CommandRunner` (standard-input / file loop).
  - `business.model`: domain objects (`User`, `UserField`, `Group`, `ParkTicket`, `Ticket`, `GroupTicket`, `Attraction`); `business`: `UserValidator`.
  - `business.controller`: controllers — `UserController`, `GroupController`, `TicketController`, `AttractionController` (the former `*Service` classes, renamed) and new `ParkController` holding the system date and ticket price and the cross-entity rules (user deletion with tickets, group locks).
  - `persistence`: file repositories (`UserFileStorage`, `GroupFileStorage`, `TicketFileStorage`, `GroupTicketFileStorage`, `AttractionFileStorage`).
- `App` stays in the base package as the composition root (launch options, `--fly`, wiring); the manifest main class is unchanged. No class in the default package.
- `uml/model.puml` reflects the packages and renamed classes.
- **No** change to command syntax, responses, data files or rules.

## Capabilities

### New Capabilities
- `application-layering`: the package structure and the dependency rules between layers.

### Modified Capabilities
_None — all behavioural capabilities keep their requirements._

## Impact

- Every source and test file moves package; services renamed to controllers; tests reorganised by package with a shared `TestData` fixture.
- Data files, commands and outputs are identical (verified by the existing regression tests and a before/after comparison).
