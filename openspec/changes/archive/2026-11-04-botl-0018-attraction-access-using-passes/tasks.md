# Tasks

## 1. Pass selection and consumption

- [x] 1.1 Add `individualTicketsOf`, `selectSpeedPasses`, `selectSpecialPass` and `consume` to `PassService`; verify `PassSelectionTest` (earliest unused chosen, missing pass rejected without changes, consume persists and reverts on failure)

## 2. Entry flow

- [x] 2.1 Implement `ParkController.enterAttraction(attraction, ticket, speed)` with `EntryResult` (SPECIAL: reject SPEED, require/consume Special Pass; SPEED: one per individual ticket; consume only after successful admission); verify `PassAccessTest` for all spec scenarios incl. group atomicity, capacity failure and restart persistence

## 3. Command and documentation

- [x] 3.1 Accept `ATTRACTION ENTER;…;SPEED`, report consumed passes, update catalog syntax and UML (`EntryResult`); verify `PassAccessCommandsTest`, `UmlModelTest`, `plantuml -checkonly`, `mvn package`, and the F3 regression script unchanged except the HELP syntax line
