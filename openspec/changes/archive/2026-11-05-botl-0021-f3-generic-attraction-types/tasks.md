# Tasks

## 1. Capabilities and generic attractions

- [x] 1.1 Add `GeneralAccess`, `IndividualAccess` and `GroupAccess`, implemented by the ticket types, and make `Attraction<T extends GeneralAccess>` abstract with the four subclasses, `AttractionType` and `PassLookup`; verify unit tests for the acceptance matrix and the generic bound

## 2. Entry and creation

- [x] 2.1 Use the generic admission flow in `ParkController.enterAttraction` (kind restriction, Special Pass requirement) and typed `createAttraction`; verify the entry scenario tests
- [x] 2.2 Accept the optional type in `ATTRACTION CREATE`, show the type in `ATTRACTION STATUS`, and add the typed syntax to `CommandCatalog`; verify command tests

## 3. Persistence and documentation

- [x] 3.1 Persist the type with GENERAL as the default for 3-field lines, and update `uml/model.puml`; run `mvn -q package` and verify all tests pass
