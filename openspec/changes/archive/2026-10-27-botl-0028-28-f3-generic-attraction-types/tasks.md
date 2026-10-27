# Tasks

## 1. Generic model

- [x] 1.1 Add the capability interfaces, `PassLookup`, `AttractionType`, the generic abstract `Attraction<T>` and the four subclasses; implement the interfaces in the ticket classes; verify with `AttractionTest` (acceptance per type, special pass condition, typed admit, generic bounds via reflection)

## 2. Service, persistence and commands

- [x] 2.1 Update `AttractionService` (typed creation, generic admission, injected pass lookup), `AttractionFileStorage` (type field, missing type → GENERAL) and all usages; verify with `AttractionServiceTest` and `ParkStorageTest`
- [x] 2.2 Accept the optional `<type>` in `ATTRACTION CREATE`, add the typed syntax to `CommandCatalog`, update `uml/model.puml`; verify with `CommandProcessorTest`, `HelpGeneratorTest`, `UmlModelTest`, PlantUML check and `mvn package`
