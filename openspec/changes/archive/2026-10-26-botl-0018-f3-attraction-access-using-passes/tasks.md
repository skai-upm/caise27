# Tasks

## 1. Model

- [x] 1.1 Add `Admission.getIndividualTickets()` (individual → itself, group → composition) and the attraction pass policy (`allowsSpeed`, Special Pass requirement in `SpecialAttraction`); verify with `AttractionTest`

## 2. Entry rules

- [x] 2.1 Add `PassManager.consumeAll`, inject `PassManager` into `AttractionManager` (reorder `Bootstrap`), and extend entry with the SPEED option and Special Pass rules, validating everything before entering and consuming; verify with `AttractionManagerTest` covering every new attractions scenario (individual/group SPEED, missing pass, atomic failures, special consumption, SPEED rejected)
- [x] 2.2 Accept the optional `SPEED` parameter in `CommandProcessor` (`ERROR INVALID OPTION` otherwise) and register the syntax; verify with `CommandProcessorTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` (new associations) and verify `plantuml -checkonly` and `UmlModelTest`
- [x] 3.2 Verify end to end with `mvn package` and a command file covering normal, SPEED (individual and group) and SPECIAL entries with consumption persisted after restart
