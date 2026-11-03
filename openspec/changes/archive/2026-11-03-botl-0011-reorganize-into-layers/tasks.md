# Tasks

## 1. Package moves

- [x] 1.1 Move model classes to `business.model`, `UserValidator` to `business`, services to `business.controller` renamed `*Controller`, storages to `persistence`, and `CommandProcessor` to `presentation`, updating packages/imports; verify `mvn compile` succeeds and no file is in the default package

## 2. Layer separation

- [x] 2.1 Extract `ParkController` (system date, price with validation, cross-entity rules) from `CommandProcessor`, and move the read loop from `App` to `presentation.CommandRunner`; verify by `grep` that presentation imports no persistence class and that `business.model` imports no persistence/presentation/file API

## 3. Tests

- [x] 3.1 Move tests to the matching packages with a shared `TestData` fixture and add `ParkControllerTest` (date, price validation, deletion/lock rules) and a `LayeringTest` asserting the import rules; verify `mvn clean test` passes with no fewer tests than before

## 4. UML and regression

- [x] 4.1 Update `uml/model.puml` with the packages and renamed/new classes (same notation); verify `plantuml -checkonly`
- [x] 4.2 Verify behaviour preservation: run a scripted E1/E2 command file with the JAR of the previous commit and the new JAR on copies of the same data and compare outputs and data files byte by byte; `mvn package` produces the JAR
