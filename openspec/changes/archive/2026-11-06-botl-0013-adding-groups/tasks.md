# Tasks

## 1. Model and errors

- [x] 1.1 Add `ParkException` (base of `UserException`) and catch it in `CommandProcessor`; verify the existing suite passes
- [x] 1.2 Add `es.upm.park.model.Group` (id, name, member DNIs, no locked flag) and `GroupRepository`, and verify with unit tests for membership operations and lookup

## 2. Service

- [x] 2.1 Add `GroupService` (create/add/remove/delete with the active-ticket predicate) and verify with unit tests: empty creation, duplicate id, add existing users, duplicate member, unknown group/user, remove/non-member, locked add/remove/delete rejected with state unchanged, delete when unlocked

## 3. Commands

- [x] 3.1 Expose the group service from `ParkController` (predicate `g -> false` until group tickets exist), dispatch the four `GROUP` commands with field-count checks, list them in `HELP`, and verify with `CommandProcessor` unit tests

## 4. Integration

- [x] 4.1 Run `mvn package` and verify all tests pass; run the JAR with a create-users/create-group/add/duplicate-add/remove/delete sequence and verify the outputs
- [x] 4.2 Run `openspec validate botl-0013-adding-groups --strict` and verify the change is valid
