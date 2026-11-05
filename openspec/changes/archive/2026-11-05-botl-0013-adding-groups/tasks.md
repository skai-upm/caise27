# Tasks

## 1. Model and storage

- [x] 1.1 Create `model.Group` and `repository.GroupRepository`; expose groups from `Park` (reset in `start`); verify unit tests for group membership operations and repository lookups

## 2. Service and commands

- [x] 2.1 Create `service.GroupService` with create/add/remove rules (unique id, existing user, no duplicate member, member required for removal); verify unit tests for each acceptance and rejection
- [x] 2.2 Register `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE` in `CommandProcessor` (with HELP lines) and wire services from `Park` in `App`; verify command-level tests
- [x] 2.3 Remove deleted users from their groups in `UserService.deleteUser`; verify a unit test

## 3. Integration

- [x] 3.1 Run the JAR with the assignment's group examples and rejections; run `mvn clean package` and verify all tests pass
