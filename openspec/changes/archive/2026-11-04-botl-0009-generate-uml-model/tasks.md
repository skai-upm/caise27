# Tasks

## 1. Model

- [x] 1.1 Write `uml/model.puml` from the current fields: datatype attributes only, no methods, relationships named after fields with crossed/diamond holder ends and multiplicities, inheritance, and usage arrows only for classes without incoming relationships; verify PlantUML `-syntax` reports a class diagram and every relationship label matches a field in the holder class
- [x] 1.2 Remove `docs/uml/model.puml`; verify no other file references it (`grep -r docs/uml` outside `openspec/changes/archive` returns nothing)

## 2. Guard test

- [x] 2.1 Add `UmlModelTest` asserting every production type in `src/main/java/es/upm/etsisi/amusementpark` is declared in `uml/model.puml` and the file has `@startuml`/`@enduml`; verify it passes, and fails when a class name is removed from the diagram (checked once manually)

## 3. Integration checks

- [x] 3.1 Run `mvn clean test` and confirm all previous tests still pass and `git diff` shows no change under `src/main`
