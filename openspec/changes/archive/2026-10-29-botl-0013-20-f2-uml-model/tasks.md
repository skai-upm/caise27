# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` with all production classes, relevant attributes, inheritance, compositions, associations, directions and multiplicities as implemented; verify by comparing each relationship against the fields of the classes

## 2. Consistency check

- [x] 2.1 Add `UmlModelTest` asserting the file exists, has `@startuml`/`@enduml`, declares every class under `src/main/java`, and contains the key relationship declarations; verify it passes and fails when a class name is removed from the diagram
- [x] 2.2 Run `mvn clean package` and verify `BUILD SUCCESS`
