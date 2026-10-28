# Tasks

## 1. Access mechanism

- [x] 1.1 Add all-or-nothing `Attraction.admit(List<User>)`; verify with model unit tests (fits, too many, already inside)
- [x] 1.2 Add `ParkController.enterAttraction` on the common ticket abstraction (existence, date equals current date, capacity); verify with controller tests including date change after entry

## 2. Command

- [x] 2.1 Implement and register `ATTRACTION ENTER` and add it to HELP; verify with command tests (success visible via ATTRACTION LIST, each rejection) and the HELP test

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR with a file using `ATTRACTION CREATE` and `ATTRACTION ENTER` with an unknown ticket; verify messages
- [x] 3.2 Commit with the exact message `[F2-C06] Add individual attraction access`; verify with `git log -1 --format=%s`
