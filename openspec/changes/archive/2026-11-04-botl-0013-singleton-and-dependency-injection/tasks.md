# Tasks

## 1. Singleton controllers

- [x] 1.1 Convert `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager` and `ParkController` to Singletons (`initialize`/`getInstance`/`reset`, private constructors) and inject `InvoiceDirector` into `ParkController`; verify `mvn test-compile`
- [x] 1.2 Wire all repositories and collaborators in `ParkSession` (dependency order, `resetControllers()` on close and on failure); verify `AppTest` normal and `--fly` runs pass

## 2. Tests and model

- [x] 2.1 Update all tests to use `initialize`/`reset`, and add `ComponentWiringTest` (same instance, second initialization fails, access before initialization fails, new session after close, injected director used); verify `mvn test`
- [x] 2.2 Update `uml/model.puml` (`ParkController.director` association); verify `UmlModelTest` and the PlantUML `-syntax` check

## 3. Regression

- [x] 3.1 Run `mvn clean package` and the E1/E2 regression script against the baseline outputs (identical)
