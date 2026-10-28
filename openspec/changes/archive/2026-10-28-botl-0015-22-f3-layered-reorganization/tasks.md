# Tasks

## 1. Packages

- [x] 1.1 Move `cli` → `presentation`, `controller` → `business.controller`, `model` → `business.model` (sources and tests) and update packages/imports and `uml/model.puml`; verify `mvn clean package` passes with all existing tests

## 2. Exceptions

- [x] 2.1 Add the `business.exception` hierarchy and `persistence.PersistenceException`; replace generic exceptions in business and persistence code; catch `ParkException` in presentation and `PersistenceException` in `App`; verify with an exception-type unit test and the existing command tests (output unchanged)

## 3. Architecture verification

- [x] 3.1 Add `ArchitectureTest` (layer packages exist, no default package, presentation/business do not import persistence, model does not import presentation); verify it passes

## 4. Delivery

- [x] 4.1 Run `mvn clean package` (BUILD SUCCESS) and the Delivery 2 command file with the JAR in normal and `--fly` mode; verify unchanged behaviour
- [x] 4.2 Commit with the exact message `[F3-C01] Reorganize application into layered packages`; verify with `git log -1 --format=%s`
