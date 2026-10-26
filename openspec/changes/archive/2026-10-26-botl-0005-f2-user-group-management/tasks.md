# Tasks

## 1. Group model and persistence

- [x] 1.1 Create `Group` (id, name, member references, add/remove/has by identity, unmodifiable member view); verify `GroupTest`
- [x] 1.2 Create `GroupRepository` (`id;name;dni1,dni2`, load resolving DNIs to loaded users, unknown DNIs dropped, missing file = empty); verify `GroupRepositoryTest` round-trip in a `@TempDir` including an empty group and an unknown DNI

## 2. Group rules

- [x] 2.1 Create `GroupManager` (create, delete, addMember, removeMember, removeUserFromAllGroups, persist with reload on failure); verify `GroupManagerTest` covers every success/failure scenario of the group-management spec, same-object membership, and restart with an email change
- [x] 2.2 Make `UserManager.delete` return the deleted user; verify `UserManagerTest` still passes

## 3. Commands

- [x] 3.1 Add GROUP CREATE/ADD/REMOVE/DELETE to `CommandProcessor` (syntax constants listed by HELP, per-command private methods) and remove deleted users from groups on USER DELETE; wire `GroupManager` with `data/groups.txt` in `App.main`; verify `CommandProcessorTest`/`AppTest` cover the GROUP commands, HELP, and the user-deletion cascade

## 4. Design model

- [x] 4.1 Create `uml/model.puml` with the current classes, attributes, associations and multiplicities including `Group`; verify `java -jar plantuml-1.2024.7.jar -checkonly uml/model.puml` succeeds
- [x] 4.2 Run `mvn package` and an end-to-end JAR run creating users and groups, restarting, and checking `data/groups.txt`
