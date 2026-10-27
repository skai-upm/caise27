# Tasks

## 1. Group model and service

- [x] 1.1 Create `Group` and `GroupRepository` and register groups in `ParkController`; verify with `GroupTest` membership add/remove/contains and unmodifiable member view
- [x] 1.2 Create `GroupService` (create, add, remove, delete, `checkModifiable` via `ParkController.hasActiveGroupTicket`) and make user deletion remove the user from groups; verify with `GroupServiceTest` success paths, duplicate id, duplicate member, unknown user/group, user not in group, and deleted user leaving groups

## 2. GROUP commands

- [x] 2.1 Dispatch `GROUP CREATE/ADD/REMOVE/DELETE` in `CommandProcessor` with exact element counts and add them to HELP; verify with `CommandProcessorTest` the source examples, error cases and HELP content, then run `mvn package` successfully
