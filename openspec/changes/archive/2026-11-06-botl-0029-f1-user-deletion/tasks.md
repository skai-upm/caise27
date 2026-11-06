# Tasks

## 1. Controller

- [x] 1.1 Add `UserController.setParkController` and `deleteUser` (locate, exists, password, park rules, remove, save), and verify with `UserControllerTest` the successful deletion, wrong password, unknown user, active ticket, locked group, removal from groups and kept historical tickets

## 2. Command and wiring

- [x] 2.1 Add the `USER DELETE` handler, wire the park controller in `AppConfiguration` and the test support, and verify with `UserCommandsTest` and an `App` restart test that the deletion persists; update the catalog/handler pending set to USER UPDATE

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
