# Tasks

## 1. Group entry

- [x] 1.1 Add `ParkControllerAccessTest` cases for group tickets (all members enter, insufficient capacity admits nobody, wrong date, one member already inside admits nobody), and verify that they pass with the shared `enterAttraction`

## 2. Exit and status

- [x] 2.1 Add `ParkController.exitAttraction` (unknown attraction or user not inside are rejected), and verify with tests that a place is released and group members leave individually
- [x] 2.2 Add the ATTRACTION EXIT and ATTRACTION STATUS handlers (status computed from the attraction) and HELP lines, and verify with `AttractionCommandsTest` the status after mixed individual/group entries and an exit, the empty status, the errors and HELP

## 3. Documentation

- [x] 3.1 Document ATTRACTION EXIT/STATUS in `README.md`, and verify that `mvn clean package` passes and that a JAR file-mode run with group entry, exit and status prints the expected counts
