# Tasks

## 1. Capabilities and generic attractions

- [x] 1.1 Add `Admission`, `IndividualAccess`, `GroupAccess` and implement them in `Ticket`, `IndividualTicket`, `GroupTicket`; verify compilation and existing ticket tests
- [x] 1.2 Make `Attraction<T extends Admission>` generic (accepted `Class<T>`, `admit`, typed `enter`, hook) with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`; verify `AttractionTypesTest` for accepted/rejected kinds per type, constituents in INDIVIDUAL, and existing `AttractionTest` adapted to `GeneralAttraction`

## 2. Service, persistence and commands

- [x] 2.1 `AttractionService.create` with optional type, repository with type field and legacy 3-field lines, controller `admit`, `ATTRACTION CREATE` 3/4 parameters, STATUS type line, catalog syntax; verify `AttractionTypeCommandsTest` (default GENERAL, explicit types, unknown type, GROUP rejects T001, INDIVIDUAL rejects TG01, legacy line → GENERAL, restart keeps type)

## 3. Documentation and regression

- [x] 3.1 Update `uml/model.puml` (interfaces, realizations, interface extension, generic hierarchy) and extend `UmlModelTest` for interface extension; verify `plantuml -checkonly`, `mvn package`, and the F3 regression script differs from the baseline only by the STATUS type lines, the HELP line and the type field in `attractions.txt`
