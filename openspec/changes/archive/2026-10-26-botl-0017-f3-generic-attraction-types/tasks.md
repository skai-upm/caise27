# Tasks

## 1. Capabilities and generic attractions

- [x] 1.1 Add `Admission`, `IndividualAdmission`, `GroupAdmission` and implement them in the ticket classes; verify existing ticket tests still pass
- [x] 1.2 Make `Attraction<T extends Admission>` abstract with the type check, typed `checkAccess` hook and admission logic; add `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`; verify `AttractionTest` for all 4 types × 2 ticket kinds, atomicity and capacity unchanged, and `AttractionType.parse/create`

## 2. Controller, persistence and commands

- [x] 2.1 Add the type to `AttractionController.create`, `AttractionRepository` (`id;name;capacity;TYPE`, 3-field lines as GENERAL) and the command registry (optional type parameter, status `Type:` line); verify `AttractionControllerTest`, `AttractionRepositoryTest` and `CommandProcessorTest` (default GENERAL, explicit types, unknown type, rejected ticket kinds, restart keeps type)

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` with the interfaces, generic hierarchy and enum; verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.2 Run `mvn package`, the regression script (only HELP/STATUS additions may differ) and an end-to-end JAR scenario with every type; verify outputs
