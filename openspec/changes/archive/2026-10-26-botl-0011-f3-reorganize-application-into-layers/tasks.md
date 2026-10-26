# Tasks

## 1. Package move

- [x] 1.1 Move main and test classes into `presentation`, `business.model`, `business.controller` and `persistence` packages (git mv, package declarations, explicit imports, cross-package visibility); verify `mvn compile` and that no file remains in `es/upm/parque` root or the default package

## 2. Business controller

- [x] 2.1 Create `ParkController` with system date/price and the cross-entity rules and `load(DataDirectory)`; slim `CommandProcessor` to parsing/formatting delegating to it; `App` builds the controller via `ParkController.load`; verify `CommandProcessor` imports nothing from `persistence` or `java.nio.file`, model classes import no I/O, and `mvn test` passes with all existing tests (adapted only to the new constructors/packages)

## 3. Verification and model

- [x] 3.1 Run an E1/E2 command script with `java -jar target/parque.jar --fly script.txt` and compare its output with the same script on the pre-change build; verify identical output
- [x] 3.2 Update `uml/model.puml` with the packages and `ParkController`; verify `plantuml -checkonly` and the field cross-check pass
