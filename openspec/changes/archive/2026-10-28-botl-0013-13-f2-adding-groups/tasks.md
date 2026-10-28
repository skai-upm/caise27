# Tasks

## 1. Group domain

- [x] 1.1 Rename `UserOperationException` to `OperationException` across sources; verify `mvn test` passes
- [x] 1.2 Create `model.Group` and `repository.GroupRepository`; verify unit tests for membership uniqueness and lookup
- [x] 1.3 Create `service.GroupService` (create, add, remove, delete, remove-user-everywhere) with the derived lock via `ActiveGroupTicketCheck`; verify unit tests for every rule including a stub check reporting an active ticket (add/remove/delete rejected) and none (allowed)

## 2. Commands

- [x] 2.1 Expose groups from `ParkController` (lock check currently reporting no active tickets) and add `ParkController.deleteUser` removing the user from groups; verify unit test
- [x] 2.2 Handle `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE` in `CommandProcessor` and list them in `HELP`; verify processor tests for success and error responses
- [x] 2.3 Run `mvn package` and a JAR session with the statement's group examples; verify `BUILD SUCCESS` and responses

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F2-C02] Add user groups`; verify with `git log -1 --format=%s`
