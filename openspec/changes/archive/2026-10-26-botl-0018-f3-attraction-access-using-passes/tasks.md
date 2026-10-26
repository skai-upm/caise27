# Tasks

## 1. Model

- [x] 1.1 Add `Admission.getIndividualTickets()`, `IndividualAdmission.getSpecialPass()`, `IndividualTicket.nextUnusedSpeedPass()` and the `EntryResult` record; verify `AttractionPassEntryTest` (individual tickets of admissions, next unused SpeedPass)
- [x] 1.2 Rework `Attraction.admit(Admission, boolean speed)` with `passesFor`/`allowsSpeed` (validate first, consume after admission) and the `SpecialAttraction` Special Pass rule; verify `AttractionPassEntryTest` for individual/group × GENERAL/INDIVIDUAL/GROUP/SPECIAL × normal/SPEED, missing passes, atomic group failure, capacity failure consuming nothing, special pass consumed only on success

## 2. Controller and command

- [x] 2.1 Add the speed flag to `AttractionController.enter` with persistence of consumed passes; register the `SPEED` syntax and report consumed passes in `CommandProcessor`; verify `AttractionControllerTest` and `CommandProcessorTest` (outputs, invalid fourth value, consumption persisted across restart)

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` (`EntryResult`, new associations); verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.2 Run `mvn package`, the regression script (only the new HELP line may differ) and an end-to-end JAR scenario with SpeedPass and Special Pass entries; verify outputs and `data/passes.txt`
