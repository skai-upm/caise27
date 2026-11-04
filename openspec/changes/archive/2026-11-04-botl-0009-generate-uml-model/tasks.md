# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` from the current sources (attributes, inheritance, associations/aggregation/composition with crosses, names and multiplicities, usage only for classes without incoming relationships, no methods/labels) and delete `docs/uml/model.puml`; verify with the PlantUML `-syntax` check
- [x] 1.2 Add `UmlModelTest` checking class-list parity with `src/main/java`, absence of methods/labels and the `@startuml`/`@enduml` wrapper; verify it passes and fails when a class is removed from the diagram

## 2. Regression

- [x] 2.1 Confirm no production file changed (`git diff --stat src/main`) and `mvn clean package` passes the full suite
