# Tasks

## 1. Update operation

- [x] 1.1 Add `UserField` and `ParkController.updateUser` (credentials, enum field, creation rules, e-mail re-indexing); verify with controller tests for every field, DNI rejection, wrong credentials, invalid/duplicate values and group membership after an e-mail change
- [x] 1.2 Add and register `UserUpdateCommand`; verify with an integration test applying the source examples and checking the persisted state after restart
- [x] 1.3 Add `UserField` to `uml/model.puml`; verify `UmlModelTest` and the PlantUML syntax check

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with create/update commands in `--fly` mode; verify output
- [x] 2.2 Commit with the exact message `[F1-C06] Implement user update`; verify with `git log -1 --format=%s`
