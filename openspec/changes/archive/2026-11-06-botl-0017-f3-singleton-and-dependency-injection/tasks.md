# Tasks

## 1. Persistence injection

- [x] 1.1 Change `TextParkStorage` to receive its three repositories by constructor and update callers, and verify that `TextParkStorageTest` passes with repositories built in the test

## 2. Controllers

- [x] 2.1 Add the `InvoiceController` singleton with an injected `ParkController`, move invoice direction from `ParkController` (exposing `groupTicketsOf` and `userDni`), and verify with tests the same-instance property, the existing invoice results, and the error when not configured
- [x] 2.2 Give `CommandProcessor` constructor injection of `ParkController` and `InvoiceController`, and verify that all command tests pass

## 3. Composition

- [x] 3.1 Add `AppConfiguration.configure(dataDir, warnings)` and use it from `App` for normal and `--fly` mode, and verify with `AppConfigurationTest` that two directories get distinct repository instances writing only to their own directory, and that `AppTest`/`Delivery2IntegrationTest` still pass
- [x] 3.2 Update `uml/model.puml` (InvoiceController, injection relationships) and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
