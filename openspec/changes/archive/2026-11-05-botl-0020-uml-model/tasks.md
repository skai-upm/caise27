# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` with classes, enums, interfaces, attributes, inheritance, implementations, associations, compositions, directions and multiplicities matching the code; verify `java -jar plantuml.jar -checkonly uml/model.puml` reports no error

## 2. Consistency

- [x] 2.1 Add `UmlModelTest` asserting all model classes and key relationships are present and no association starts at `User`; verify `mvn clean package` passes
