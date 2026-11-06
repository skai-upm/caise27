# Tasks

## 1. Controller

- [x] 1.1 Add the `UserController` singleton with injected registry and password service and `createUser` (validation, uniqueness, protection, storage), and verify with `UserControllerTest` a valid creation (protected password verifies), duplicate DNI, duplicate email, each invalid field, and that a failure adds no user

## 2. Command and wiring

- [x] 2.1 Add the `USER CREATE` handler, the `CommandProcessor` constructor injection and the `AppConfiguration`/test-support wiring, and verify with `UserCommandsTest` the confirmation, the errors (including wrong field count), and that a created user can buy a ticket; update the catalog/handler test pending set to USER DELETE and USER UPDATE

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
