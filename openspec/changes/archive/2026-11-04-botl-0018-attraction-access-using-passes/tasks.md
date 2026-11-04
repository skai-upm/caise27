# Tasks

## 1. Pass consumption

- [x] 1.1 Add `PassManager` selection helpers (first unconsumed SpeedPass / Special Pass of a ticket) and atomic `consume(List<Pass>)` with rollback; verify with `PassManagerTest`

## 2. Entry orchestration

- [x] 2.1 Add `EntryResult` and `ParkController.enterAttraction(aid, tid, speed)` with preflight, entry, consumption and rollback; verify with `ParkControllerTest` (SPEED individual/group, missing SpeedPass atomic, SPECIAL automatic, SPEED at SPECIAL rejected, capacity/date failures keep passes)
- [x] 2.2 Parse the optional `SPEED` option in `CommandProcessor`, report consumed passes, add the catalog entry; verify with `CommandProcessorTest` and an `AppTest` restart showing persisted consumption

## 3. Model docs and regression

- [x] 3.1 Update `uml/model.puml`; verify `UmlModelTest` and the PlantUML `-syntax` check
- [x] 3.2 Run `mvn clean package` and the E1/E2 regression script (identical apart from HELP and STATUS type lines)
