# Tasks

## 1. Attraction model

- [x] 1.1 Create `Attraction` (occupants as users, unmodifiable view, available places) and `AttractionRepository`, register them in `ParkController`; verify with `AttractionTest` and a reflection check that `User` has no attraction field

## 2. Commands

- [x] 2.1 Create `AttractionService` and dispatch `ATTRACTION CREATE` / `ATTRACTION LIST` in `CommandProcessor` (capacity validation, empty-list message) with HELP lines; verify with `CommandProcessorTest` the source examples, duplicate id, invalid capacities, empty list, unknown attraction, and run `mvn package` successfully
