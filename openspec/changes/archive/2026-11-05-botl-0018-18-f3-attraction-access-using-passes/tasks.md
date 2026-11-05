# Tasks

## 1. Pass availability and consumption

- [x] 1.1 Add `PassManager.findAvailableSpeedPass/findAvailableSpecialPass` and atomic persisted `consume(List<Pass>)`, plus `Attraction.allowsSpeed/requiresSpecialPass` overridden by `SpecialAttraction`; verify with `PassManagerTest` (selection skips consumed, consumption persisted across restart) and `AttractionTypeTest`

## 2. Entry orchestration

- [x] 2.1 Implement `ParkController.enterAttraction(aid, tid, speed)` and `ATTRACTION ENTER;...;SPEED` parsing plus catalog line; verify with `CommandProcessorTest` covering S1–S5 (single SpeedPass consumed and named, group atomicity with a member lacking a pass, capacity failure consumes nothing, Special Pass auto-consumed and reported, SPEED rejected at SPECIAL, normal entry consumes nothing, invalid third parameter) and `mvn test`

## 3. Regression

- [x] 3.1 Diff the E1/E2 script output with the previous JAR (only the new HELP line may differ), run the layer check, and verify `uml/model.puml` still matches the code (PlantUML `-checkonly` and cross-check)
