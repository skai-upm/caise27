# Tasks

## 1. Model

- [x] 1.1 Create `Attraction` (identifier, name, capacity, unmodifiable occupants with capacity-bounded enter/leave) and add attraction storage with `createAttraction` to `ParkController`; verify unit tests for creation, duplicates, invalid fields and capacity bounds

## 2. Commands

- [x] 2.1 Implement `ATTRACTION CREATE` and `ATTRACTION LIST`, register them in `App.run`, and list them in `HELP`; verify command tests for occupant listing, empty result, unknown attraction, invalid capacity and an unchanged state after listing

## 3. Verification

- [x] 3.1 Run `mvn -q package` and verify all tests pass
