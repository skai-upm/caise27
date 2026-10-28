# Tasks

## 1. Model

- [x] 1.1 Add `Admission`, `IndividualAdmission`, `GroupAdmission` and make the tickets implement them; verify compilation and existing tests
- [x] 1.2 Make `Attraction` generic and abstract with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`; verify with model tests (accepted/rejected ticket types, Special Pass rule, generic bounds via reflection)

## 2. Controller, commands and persistence

- [x] 2.1 Support typed creation and type-restricted entry in `ParkController`; verify with controller tests
- [x] 2.2 Accept 3 or 4 fields in `ATTRACTION CREATE`, show the type in `ATTRACTION STATUS`, add catalogue entry; verify with command and HELP tests
- [x] 2.3 Store the type in the attraction file and read untyped lines as GENERAL; verify with persistence tests
- [x] 2.4 Update `uml/model.puml` and `UmlModelTest` for interfaces, enum and generic attractions; verify the UML test and PlantUML syntax check

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with typed attractions in `--fly` mode; verify outputs
- [x] 3.2 Commit with the exact message `[F3-C07] Add generic attraction access types`; verify with `git log -1 --format=%s`
