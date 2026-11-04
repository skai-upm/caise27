# Tasks

## 1. Model

- [x] 1.1 Add `Admissible`, `IndividualAccess`, `GroupAccess` and make the tickets implement them; verify `mvn test` unchanged
- [x] 1.2 Turn `Attraction` into `Attraction<T extends Admissible>` with typed enter/exit and `enterAny`/`exitAny`, add the four subclasses and `AttractionType`; verify with `AttractionTest` (accepted and rejected kinds, existing capacity/date rules) and a compile-level usage of the typed API

## 2. Service, persistence and commands

- [x] 2.1 Update `AttractionManager` (create with type), `AttractionFileStorage` (`id;name;capacity;type`, legacy 3 fields as GENERAL), `ParkController` and `CommandProcessor` (optional type, STATUS type line, rejection message), `CommandCatalog`; verify with `AttractionManagerTest`, `CommandProcessorTest` and a storage test for legacy lines

## 3. Model docs and regression

- [x] 3.1 Update `uml/model.puml` and make `UmlModelTest` handle generic declarations; verify `UmlModelTest` and the PlantUML `-syntax` check
- [x] 3.2 Run `mvn clean package` and the E1/E2 regression script (identical except HELP lines and the STATUS type line), including reading the baseline attractions file
