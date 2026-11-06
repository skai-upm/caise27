# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` with the model, controller, persistence and command packages, their relevant attributes, inheritance, implementations, compositions/associations with directions and multiplicities, and the users-by-email note, and verify with `java -jar plantuml.jar -checkonly uml/model.puml` that the syntax is valid
- [x] 1.2 Review every relationship in the diagram against the code (field types, collections, nullability), and verify that no element without a counterpart in the code is shown

## 2. Consistency check and docs

- [x] 2.1 Add `UmlModelConsistencyTest` asserting that every type of the model, controller and persistence packages is declared in `uml/model.puml`, and verify that it passes and fails when a name is removed
- [x] 2.2 Reference the UML model in `README.md`, and verify that `mvn clean package` passes
