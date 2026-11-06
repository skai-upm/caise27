# Tasks

## 1. Package reorganization

- [x] 1.1 Move `command` to `presentation`, `model` to `business/model` and `controller` to `business/controller` (exceptions to `business/exception`) with `git mv` for sources and tests, update package and import declarations, and verify that `mvn -q clean test` compiles and all existing tests pass
- [x] 1.2 Move command-file reading from `App` into `presentation.CommandRunner`, keep `App` as the composition root, and verify that `AppTest` and `Delivery2IntegrationTest` pass unchanged

## 2. Exceptions

- [x] 2.1 Add the `ParkException` hierarchy (`NotFoundException`, `BusinessRuleException`, `InvalidInputException`, `StorageException`) and use the specific types at every throw site, and verify with tests that unknown elements throw `NotFoundException` and rule violations throw `BusinessRuleException`
- [x] 2.2 Wrap persistence write failures in `StorageException`, and verify with a test using a failing storage that the error is reported and the next command is still processed

## 3. Architecture and documentation

- [x] 3.1 Add `LayeredArchitectureTest` (package declarations, forbidden imports), and verify that it passes and fails when a forbidden import is introduced
- [x] 3.2 Update `uml/model.puml` to the layer packages and exception hierarchy and `UmlModelConsistencyTest` to the new source directories, and verify with the PlantUML syntax check and the test suite, then describe the layers in `README.md`
