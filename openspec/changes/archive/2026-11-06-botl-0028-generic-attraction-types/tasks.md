# Tasks

## 1. Model

- [x] 1.1 Add `Admission`, `IndividualAccess`, `GroupAccess` (implemented by the tickets), `PassLookup`, generic `Attraction<T>` with `General/Individual/Group/SpecialAttraction` and `AttractionType`; adapt existing uses; verify with unit tests that each attraction admits exactly its ticket types, uses its bound in `holdersOf`, and the special requirement

## 2. Service, persistence, commands

- [x] 2.1 Update `AttractionService` (typed creation, generic admission in `enter`) and `ParkController` (pass lookup), and verify with unit tests for every `attraction-types` scenario
- [x] 2.2 Store and load the attraction type (legacy lines as GENERAL) and verify with store tests
- [x] 2.3 Support 3–4 fields for `ATTRACTION CREATE`, update catalog/HELP/README, and verify with `CommandProcessor` tests (types, legacy syntax, invalid type)

## 3. Documentation and integration

- [x] 3.1 Add interfaces, realizations and the attraction hierarchy to `uml/model.puml`, extend `UmlModelTest` (interfaces, `<|..`), and verify PlantUML `-checkonly` and the test pass
- [x] 3.2 Run `mvn package` and verify all tests pass; run the JAR with the four example attractions and accepted/rejected entries
- [x] 3.3 Run `openspec validate botl-0028-generic-attraction-types --strict` and verify the change is valid
