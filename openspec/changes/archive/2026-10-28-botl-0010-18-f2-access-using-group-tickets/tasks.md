# Tasks

## 1. Group access verification

- [x] 1.1 Add controller tests for group-ticket entry (whole group, insufficient capacity → nobody, wrong date, member already inside); verify they pass on the common access operation

## 2. Exit and status

- [x] 2.1 Add `Attraction.findOccupant`/`getAvailablePlaces` and `ParkController.exitAttraction`; verify with model/controller tests (exit releases capacity, group member exits alone, not inside, unknown attraction)
- [x] 2.2 Implement and register `ATTRACTION EXIT` and `ATTRACTION STATUS`, add them to HELP; verify with command tests (status fields after mixed entries and an exit) and the HELP test

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR with a file creating an attraction and calling `ATTRACTION STATUS` and an invalid `ATTRACTION EXIT`; verify output
- [x] 3.2 Commit with the exact message `[F2-C07] Add group attraction access`; verify with `git log -1 --format=%s`
