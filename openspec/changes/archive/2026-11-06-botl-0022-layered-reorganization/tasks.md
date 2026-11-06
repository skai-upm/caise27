# Tasks

## 1. Reorganization

- [x] 1.1 Move sources and tests to `presentation`, `business.{model,service,controller}`, `persistence`, `persistence.repository`, rewrite package/import declarations, and update the manifest main class; verify `mvn package` passes with the unchanged behavioral tests
- [x] 1.2 Update `uml/model.puml` package names and `UmlModelTest` lookup packages; verify the UML test and PlantUML `-checkonly` pass

## 2. Guards

- [x] 2.1 Add `ArchitectureTest` (layer packages, no default/root package classes, forbidden imports) and verify it passes and fails when a forbidden import is introduced
- [x] 2.2 Add a `MainTest` case with an unknown command, a rejected business operation and a valid command in a file, and verify errors are reported and processing continues

## 3. Integration

- [x] 3.1 Run `mvn clean package` and verify all tests pass; run the JAR with the Delivery 2 file normally and with `--fly` and verify the output equals the pre-reorganization output
- [x] 3.2 Run `openspec validate botl-0022-layered-reorganization --strict` and verify the change is valid
