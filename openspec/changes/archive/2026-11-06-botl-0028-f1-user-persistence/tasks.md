# Tasks

## 1. Repository

- [x] 1.1 Add `UserRepository`, inject it into `ParkStorage` (loaded first), and add `ParkController.restoreUser`; verify persistence tests for restart recovery, field mapping, no plain password in the file, and malformed or duplicate lines

## 2. Verification

- [x] 2.1 Update the UML, run `mvn -q package`, and run the JAR twice (create a user, then restart and buy a ticket for them); verify all tests pass and the user survives
