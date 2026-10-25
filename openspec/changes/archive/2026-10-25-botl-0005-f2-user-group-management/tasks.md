# Tasks

## 1. Group model and persistence

- [x] 1.1 Create `Group` (id, name, ordered members, add/remove/contains, unmodifiable view) and add `UserManager.findByDni`; verify with `GroupTest` and a new `UserManagerTest` case
- [x] 1.2 Create `GroupRepository` (`data/groups.txt` format `<id>;<name>;<dni,dni>`, load with a DNI lookup ignoring unknown DNIs and malformed lines, save rewriting the file); verify with `GroupRepositoryTest` using `@TempDir`

## 2. Group operations and commands

- [x] 2.1 Create `GroupManager` (create/add/remove/delete with all error responses, `removeUserFromAll`, persistence after each success); verify with `GroupManagerTest` covering every spec scenario including restart and same-object membership
- [x] 2.2 Add the four GROUP commands to `CommandProcessor` (parameter counts, HELP list) and the user-deletion cascade; wire `GroupManager` with `data/groups.txt` in `App`; verify with `CommandProcessorTest`/`AppTest` and that all Delivery 1 tests still pass

## 3. Design model

- [x] 3.1 Write `uml/model.puml` reflecting the current classes, enum, associations and multiplicities including `Group`–`User`; verify it with `java -jar plantuml.jar -checkonly uml/model.puml`

## 4. Integration

- [x] 4.1 Verify end to end with `mvn package` and a command file that creates users and groups, adds/removes members, deletes a member user, exits, and a second run showing groups restored
