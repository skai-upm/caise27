# Tasks

## 1. Domain hooks

- [x] 1.1 Add `Ticket.getIndividualTickets()` and the `acceptsSpeed()`/`requiresSpecialPass()` hooks (overridden by `SpecialAttraction`); add `PassController` helpers (first unused SpeedPass, Special Pass of a ticket, `consume(list)` with save); verify with unit tests

## 2. Entry with passes

- [x] 2.1 Implement pass-aware `AttractionController.enter(..., speed)` returning `EntryResult`, inject `PassController`, reorder configuration in `App`; verify with unit tests for every spec scenario (individual/group SPEED, atomic failure, capacity failure, special pass present/consumed/missing, SPEED at SPECIAL)
- [x] 2.2 Accept the optional `SPEED` field in `CommandProcessor`, report consumed passes, update the catalog; verify with command tests, the regression script (unchanged except HELP) and a jar session with a restart showing consumption persisted
- [x] 2.3 Update `uml/model.puml`; verify with `plantuml -checkonly`
