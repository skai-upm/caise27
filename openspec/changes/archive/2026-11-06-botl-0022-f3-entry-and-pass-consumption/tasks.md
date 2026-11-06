# Tasks

## 1. Model

- [x] 1.1 Add `AccessTicket.individualTickets()`, extend `EntryRules` (available SpeedPass/Special Pass, consume), and change `Attraction.admit` to the validate-then-mutate flow with the SPEED option and `EntryResult`, with `SpecialAttraction` consuming its pass and rejecting SPEED, and verify with model tests (individual SPEED, group SPEED atomicity, special consumption, failures leave passes unchanged)

## 2. Controller and command

- [x] 2.1 Implement `EntryRules` in `ParkController.enterAttraction(…, speed)` (save after consumption), parse the optional `SPEED` argument in `CommandProcessor` with consumed-pass output, and add the catalog entry, and verify with `PassEntryCommandsTest` the spec scenarios, including persistence of the consumed flag through a storage round-trip

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
