# Tasks

## 1. Diagram

- [x] 1.1 Write `uml/model.puml` (model classes with relevant attributes, `UserField` enum, ticket hierarchy, `GroupTicket` composition, directed associations with multiplicities, `ParkController` single instance and owned elements) and verify it with PlantUML `-checkonly`

## 2. Consistency

- [x] 2.1 Add `UmlModelTest` checking classes, enums, abstract markers, inheritance and associations of `uml/model.puml` against the code, and verify it passes (and fails if a relationship is altered)

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass
- [x] 3.2 Run `openspec validate botl-0020-uml-model --strict` and verify the change is valid
