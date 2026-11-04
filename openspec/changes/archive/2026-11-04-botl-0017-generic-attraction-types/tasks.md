# Tasks

## 1. Capabilities and typed attractions

- [x] 1.1 Add `AttractionAccess`, `IndividualAccess`, `GroupAccess` (implemented by `Ticket`, `IndividualTicket`, `GroupTicket`), make `Attraction<T extends AttractionAccess>` abstract with `accessType()`, `getType()` and `admit`, add the four subclasses and `AttractionType`; verify with `AttractionTypeTest` (admission matrix, typed compile-time usage, run-time rejection message, existing `AttractionTest` behaviours on `GeneralAttraction`)

## 2. Persistence, controller and commands

- [x] 2.1 Store and load the type (legacy 3-field lines as GENERAL), accept 3 or 4 parameters in `ATTRACTION CREATE`, use `admit` for entry, show `type=` in STATUS, add the catalog entry; verify with `AttractionManagerTest` and `CommandProcessorTest` cases for every scenario and `mvn clean test`

## 3. Model and integration

- [x] 3.1 Update `uml/model.puml`; verify PlantUML `-syntax`, `UmlModelTest` and the label/field check
- [x] 3.2 Run `mvn clean package` and the JAR with typed attractions and a legacy `attractions.txt`
