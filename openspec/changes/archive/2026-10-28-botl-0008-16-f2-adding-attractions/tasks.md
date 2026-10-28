# Tasks

## 1. Model and controller

- [x] 1.1 Add `Attraction` with capacity/duplicate invariants on occupants; verify with unit tests
- [x] 1.2 Add attraction registry with `createAttraction` (unique id, positive integer capacity) and `findAttraction`; verify with controller unit tests

## 2. Commands

- [x] 2.1 Implement and register `ATTRACTION CREATE` and `ATTRACTION LIST` (read-only, empty result, unknown id) and add them to HELP; verify with command tests and the HELP test

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR with a file creating `A01`, a duplicate, and listing `A01`; verify messages
- [x] 3.2 Commit with the exact message `[F2-C05] Add attractions`; verify with `git log -1 --format=%s`
