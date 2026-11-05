# Tasks

## 1. Typed attractions

- [x] 1.1 Add `AccessCredential` (implemented by `Ticket`), `AttractionType`, generic abstract `Attraction<T>` with `accepts`/typed `admit`/`checkAdditional` hook, and the four subclasses; adapt existing attraction tests; verify with `AttractionTypeTest` (each type's accepted kinds, rejection leaves nobody present, SPECIAL admits individual tickets, factory and parse)

## 2. Creation and persistence

- [x] 2.1 Support the optional type in `AttractionManager.create` and `ATTRACTION CREATE`, write/read the type in `attractions.txt` (3-field lines as GENERAL), add the catalog line; verify with `AttractionManagerTest`/`CommandProcessorTest` (typed creation, invalid types, GROUP rejects T001, INDIVIDUAL rejects TG01, legacy line, restart keeps type) and `mvn test`

## 3. Regression and documentation

- [x] 3.1 Diff the E1/E2 script output with the previous JAR (only HELP lines and the attraction record's type field may differ), run the layer check, update `uml/model.puml` (interface, generic class, subclasses, enum) and verify PlantUML `-checkonly` and the cross-check
