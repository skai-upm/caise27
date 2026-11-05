# Tasks

## 1. Model and store

- [x] 1.1 Create `model.Group` (id, name, member list, `contains`/`add`/`remove`/unmodifiable `getMembers`, membership by DNI) and `repository.GroupRepository` (in-memory, id-keyed, `removeUserEverywhere`); verify with `GroupTest`/`GroupRepositoryTest`
- [x] 1.2 Add groups and `hasActiveGroupTicket(Group)` (derived, false while no tickets) to `ParkController`, reset in `startExecution`; verify `ParkControllerTest` that groups reset per execution

## 2. Commands

- [x] 2.1 Implement `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE` in `command.GroupCommands` and dispatch from `CommandProcessor`; verify `GroupCommandsTest` for every scenario in the user-groups spec (success, duplicate id, malformed, unknown group/user, duplicate member, non-member removal, deletion, unknown delete)
- [x] 2.2 Remove a deleted user from all groups in `USER DELETE`; verify a test where a member is deleted and the group no longer contains them
- [x] 2.3 List the four group commands in `HELP`; verify the help test

## 3. Integration

- [x] 3.1 End-to-end with the JAR: create users, a group, add/remove members, duplicate add, delete group; verify responses and exit status 0; full `mvn package` regression
