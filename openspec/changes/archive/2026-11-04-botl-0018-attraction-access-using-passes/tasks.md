# Tasks

## 1. Preflight and pass lookups

- [x] 1.1 Add `Attraction.checkAdmission` (non-mutating; `admit` reuses it) and `PassManager.firstUnusedSpeedPass`, `unusedSpecialPass`, atomic `consume`; verify with `AttractionTypeTest`/`PassManagerTest` cases (check does not mutate, first unused SpeedPass in order, consume rollback on save failure)

## 2. Entry orchestration and command

- [x] 2.1 Implement the preflight → admit → consume sequence in `ParkController.enterAttraction(attraction, ticket, speed)` with compensation, the `SPEED` option in `CommandProcessor` and the catalog entry; verify with `CommandProcessorTest` cases for every `pass-based-access` scenario (including restart persistence of consumed flags) and `mvn clean test`

## 3. Integration

- [x] 3.1 Update `uml/model.puml` notes if needed and verify PlantUML `-syntax`, `UmlModelTest`; run `mvn clean package` and the JAR with SPEED and SPECIAL entries
