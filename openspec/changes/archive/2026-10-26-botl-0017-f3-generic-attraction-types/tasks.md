# Tasks

## 1. Model

- [x] 1.1 Add `Admission`, `IndividualAdmission`, `GroupAdmission` and implement them in the ticket classes; verify existing ticket tests pass
- [x] 1.2 Turn `Attraction` into generic abstract `Attraction<T extends Admission>` with the four typed subclasses and the `AttractionType` enum (factory, parse); verify with `AttractionTest` (accepts per type, cast, typed admit, capacity)

## 2. Operations and persistence

- [x] 2.1 Update `AttractionManager` (optional type on create, typed entry through the generic helper, `ERROR TICKET TYPE NOT ACCEPTED`, `Type:` line in status) and `TextFileAttractionRepository` (type field, legacy lines as GENERAL); verify with `AttractionManagerTest` and `TextFileAttractionRepositoryTest`
- [x] 2.2 Accept 3 or 4 parameters in `ATTRACTION CREATE` and register the typed syntax in `CommandRegistry`; verify with `CommandProcessorTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` and `UmlModelTest` (generic headers, interface inheritance, parameterised element types); verify `plantuml -checkonly` and `mvn test`
- [x] 3.2 Verify end to end with `mvn package` and a command file creating the four types and trying accepted and rejected entries, plus a restart keeping the types
