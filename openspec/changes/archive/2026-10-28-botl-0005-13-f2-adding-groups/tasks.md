# Tasks

## 1. Model and controller

- [x] 1.1 Add minimal `User` (encapsulated e-mail) and `Group` (identifier, name, members, no duplicate members); verify with unit tests for membership invariants
- [x] 1.2 Add user and group registries plus group operations and derived `isGroupModifiable` to `ParkController` (reset clears them); verify with controller unit tests (duplicate id, unknown user/group, duplicate member, non-member removal)

## 2. GROUP commands

- [x] 2.1 Implement and register `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE` with field-count/empty checks; verify with command tests covering success and every rejection
- [x] 2.2 Add the three commands to HELP; verify with the HELP unit test

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR with a file using `GROUP CREATE;G01;Familia Garcia`, a duplicate create and `GROUP ADD` of an unknown user; verify messages
- [x] 3.2 Commit with the exact message `[F2-C02] Add user groups`; verify with `git log -1 --format=%s`
