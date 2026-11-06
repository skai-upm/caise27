# Tasks

## 1. Access logic

- [x] 1.1 Add the attraction hooks (`acceptsSpeedPasses`, `speedPassTickets`, `automaticPasses`) and `AccessResult`, extend `AttractionService.enter` with SPEED and consumption, and verify with unit tests for each `attraction-access` scenario (individual/group SPEED, missing pass, capacity failure keeps passes, special consumption, consumed special pass, SPEED at special)

## 2. Commands

- [x] 2.1 Accept `ATTRACTION ENTER` with an optional `SPEED`, print consumed passes, update catalog/HELP/README, and verify with `CommandProcessor` tests

## 2b. Constituent tickets

- [x] 2b.1 Let `ATTRACTION ENTER` resolve group-ticket constituents as individual tickets and verify with tests that a constituent with a Special Pass enters a special attraction (consuming it) while the group ticket is still rejected there

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with SpeedPass and Special Pass entries and verify outputs and invoice/refund effects of consumed passes
- [x] 3.2 Run `openspec validate botl-0029-entry-and-pass-consumption --strict` and verify the change is valid
