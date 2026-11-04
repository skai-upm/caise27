# Tasks

## 1. Cascade deletion

- [x] 1.1 Add `TicketManager.checkDeletable`/`checkGroupDeletable` (reused by the delete methods) and `PassManager.removeAll`/`restore`; replace the guard in `ParkController` with the validate → remove passes → delete ticket → restore-on-failure sequence; verify with `CommandProcessorTest`/`ParkControllerTest` cases for individual and group cascades (also after restart), invalid deletions keeping everything, and storage failure restoring passes

## 2. Persistence evidence

- [x] 2.1 Add restart tests for consumed passes (identity, price, ticket link, status) and typed attractions, and extend the `--fly` `AppTest` to passes and typed attractions; verify they pass

## 3. Model and integration

- [x] 3.1 Re-verify `uml/model.puml` against the code (PlantUML `-syntax`, `UmlModelTest`, label/field check); run `mvn clean package` and the JAR with a cascade scenario across a restart
