# Tasks

## 1. Attraction model

- [x] 1.1 Create `model/Attraction` and `repository/AttractionRepository`, expose attractions from `ParkController` (reset on `start`); verify with `AttractionTest` (occupants unmodifiable, occupancy/free places, `User` has no attraction field)

## 2. Commands

- [x] 2.1 Implement ATTRACTION CREATE and ATTRACTION LIST in `AttractionCommands`, list them in HELP; verify every attractions scenario in `AttractionCommandsTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F2-C05] Add attractions`; verify with `git log -1 --format=%s`
