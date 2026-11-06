# Tasks

## 1. Model

- [x] 1.1 Add `AccessTicket`, `IndividualAccess` and `GroupAccess`, and make `Ticket`, `IndividualTicket` and `GroupTicket` implement them, and verify that the existing model tests pass
- [x] 1.2 Make `Attraction<T extends AccessTicket>` abstract with `canHandle`/`admit`/`checkEntry`, add the four subclasses, `AttractionType` and `EntryRules`, and verify with `AttractionTypesTest` the acceptance matrix, the Special Pass requirement and all-or-nothing admission

## 2. Integration

- [x] 2.1 Update `ParkController` (typed creation, entry through `admit` with `EntryRules` backed by passes), `AttractionRepository` (type field, three-field compatibility) and `CommandProcessor`/catalog (3- or 4-argument creation, type in status), and verify with tests (controller, round-trip, commands, Delivery 2 integration)

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
