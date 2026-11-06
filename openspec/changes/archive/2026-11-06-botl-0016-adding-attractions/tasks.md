# Tasks

## 1. Model and service

- [x] 1.1 Add `Attraction` (id, name, capacity, occupants with enter/leave/isFull/contains) and `AttractionRepository`, and verify with unit tests that a new attraction exposes its data and an empty, unmodifiable occupant collection
- [x] 1.2 Add `AttractionService` (create with id/name/capacity validation, find, occupant emails) and verify with unit tests for valid creation, duplicate id, invalid capacities and listing

## 2. Commands

- [x] 2.1 Expose the service from `ParkController`, dispatch `ATTRACTION CREATE`/`ATTRACTION LIST`, list them in `HELP`, and verify with `CommandProcessor` tests that listing an empty attraction is controlled and read-only

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with create/list/invalid commands and verify outputs
- [x] 3.2 Run `openspec validate botl-0016-adding-attractions --strict` and verify the change is valid
