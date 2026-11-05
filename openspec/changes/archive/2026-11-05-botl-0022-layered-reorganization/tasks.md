# Tasks

## 1. Packages

- [x] 1.1 Move classes (and their tests) to `presentation`, `business.{model,control,service,exception}` and `persistence.{repository,storage}` with `git mv`, fix package/imports, update `pom.xml` main class and test references; verify `mvn clean package` passes and `java -jar` starts

## 2. Exceptions

- [x] 2.1 Introduce `OperationException` with `NotFoundException`, `InvalidDataException`, `RuleViolationException` and `PersistenceException`; replace generic throws and `UncheckedIOException` in storages; map them in `CommandProcessor` and `App`; verify unit tests for each category and the storage-failure message

## 3. Architecture checks

- [x] 3.1 Add an architecture test for package placement and dependency rules (presentation has no data-file I/O, model imports nothing from persistence/presentation); update UML package names; verify all tests pass and run the JAR in all modes
