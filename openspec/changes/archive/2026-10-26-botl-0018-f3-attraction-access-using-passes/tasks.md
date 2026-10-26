# Tasks

## 1. Model

- [x] 1.1 Add `getIndividualAccesses()` to `Admission` and `findAvailableSpeedPass()` to `IndividualAccess`; implement `EntryResult` and the speed-aware validate-then-commit `Attraction.enter`, with `SpecialAttraction` using `passesToConsume` and rejecting SPEED; verify with `AttractionTest` (individual/group SPEED, atomic failure, normal entry consumes nothing, SPECIAL auto-consumption, SPEED rejected at SPECIAL, full SPECIAL keeps pass)

## 2. Controller, persistence and commands

- [x] 2.1 Persist consumption through `TicketManager`, extend `ParkController.enterAttraction` with the speed flag, parse the optional `SPEED` parameter and report consumed passes in `CommandProcessor`, add the catalog entry; verify with `CommandProcessorTest`/`AppTest` (restart keeps CONSUMED) and `mvn test`
- [x] 2.2 Update `uml/model.puml` (`EntryResult`); verify `plantuml -checkonly` and the field cross-check pass
