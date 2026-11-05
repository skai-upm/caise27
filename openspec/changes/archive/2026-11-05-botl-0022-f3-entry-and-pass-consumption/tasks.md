# Tasks

## 1. Admission model

- [x] 1.1 Add `individualTicketIds()` to the capabilities, plus `Attraction.admit` with `Admission`, `acceptsSpeedPasses` and `requiredPasses` (Special Pass in `SpecialAttraction`); verify unit tests for SpeedPass selection and rejection rules

## 2. Entry

- [x] 2.1 Make `ParkController.enterAttraction` two-phase with SpeedPass and Special Pass consumption on success only; verify tests for every scenario, including no change on failure
- [x] 2.2 Accept `;SPEED` in `ATTRACTION ENTER`, print consumed passes, and add the variant to `CommandCatalog`; verify command tests

## 3. Verification

- [x] 3.1 Update `uml/model.puml`, run `mvn -q package`, and verify all tests pass
