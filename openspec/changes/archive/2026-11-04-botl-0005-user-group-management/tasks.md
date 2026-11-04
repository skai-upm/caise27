# Tasks

## 1. User references

- [x] 1.1 Make `UserManager.update` apply changes to the existing `User` after a successful save (`User.copyEditableFrom`), add `findByDni` and make `delete` return the removed user; verify existing `UserManagerTest` passes plus a test that the same object reflects an update

## 2. Groups

- [x] 2.1 Create `Group` and `GroupManager` with create/delete/add/remove rules (unique id, nonblank fields, registered users only, no duplicates, unknown group/non-member errors, atomic changes); verify with `GroupManagerTest`
- [x] 2.2 Create `GroupFileStorage` (`id;name;dni...`, missing file empty, unknown DNIs ignored) and restore groups as references to loaded users; verify with `GroupFileStorageTest` and a restart test in `GroupManagerTest` (same object identity, email change survives)

## 3. Commands

- [x] 3.1 Add `GROUP CREATE/ADD/REMOVE/DELETE` to `CommandProcessor`, remove deleted users from all groups, add the commands to HELP and wire `groups.txt` in `App`; verify with `CommandProcessorTest` cases and an `AppTest` restart run

## 4. Model and regression

- [x] 4.1 Write `docs/uml/model.puml` with current classes, associations and multiplicities; verify every declared class exists in `src/main/java`
- [x] 4.2 Run `mvn clean package` with all tests passing and a manual JAR run of the group commands across two executions
