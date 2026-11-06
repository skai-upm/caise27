# Tasks

## 1. Model and controller

- [x] 1.1 Add the `UserField` enum (DNI rejected explicitly) and `UserController.updateUser` with authentication, per-field validation, password re-protection and saving, and verify with `UserControllerTest` every field, wrong password, DNI/unknown field, invalid values and the unused-email rule
- [x] 1.2 Add `UserRegistry.rename`, `ParkController.changeUserEmail`, `Group.replaceMember`, `IndividualTicket.reassignOwner` and `Attraction.replaceOccupant`, and verify with tests that tickets (including composed ones), groups, occupants, passes and invoices follow the new email

## 2. Command and persistence

- [x] 2.1 Add the `USER UPDATE` handler, update the catalog/handler test (no pending commands), and verify with `UserCommandsTest` and an `App` restart test that updates persist

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` (UserField) and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
