# Tasks

## 1. Access interfaces and generic attractions

- [x] 1.1 Add `IndividualAccess` and `GroupAccess`, implement them in the tickets; make `Attraction<T extends Admission>` with accepted type and hooks, `SpecialAttraction`, and `AttractionType` factory; verify with `AttractionTest` (GROUP rejects individual, INDIVIDUAL rejects group, GENERAL admits both, SPECIAL requires and consumes an available Special Pass, atomicity kept)

## 2. Management, persistence and commands

- [x] 2.1 Persist the type (legacy lines → GENERAL), add the type to `AttractionManager.create`, `ParkController`, `ATTRACTION CREATE` (3 or 4 parameters), STATUS output and the catalog; verify with `AttractionManagerTest`/`CommandProcessorTest` and `mvn test`
- [x] 2.2 Update `uml/model.puml` (interfaces, generic `Attraction<T>`, `SpecialAttraction`, `AttractionType`); verify `plantuml -checkonly` and the field cross-check pass
