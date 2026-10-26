# Tasks

## 1. Group model and persistence

- [x] 1.1 Create `Group` (id, name, member list by identity) and `GroupStorage` (`Id;Nombre;DNI,...` lines, resolve DNIs via a lookup, skip unknown); verify with `GroupStorageTest` round-trip and unknown-DNI cases
- [x] 1.2 Expose `UserManager.findByEmail`/`findByDni` and make `delete` return the removed user; verify existing `UserManagerTest` still passes

## 2. Group operations and commands

- [x] 2.1 Implement `GroupManager` create/delete/add/remove/removeUserFromAll with all error rules and persistence after each change; verify with `GroupManagerTest` (duplicates, nonexistent user, same object as registered user, restart restores groups)
- [x] 2.2 Route `GROUP CREATE|ADD|REMOVE|DELETE` in `CommandProcessor`, cascade USER DELETE to groups, add group usages to HELP, and wire `data/groups.txt` in `App`; verify with `CommandProcessorTest`/`AppTest` cases and `mvn test`

## 3. Domain model

- [x] 3.1 Write `docs/uml/model.puml` with all current classes, associations and multiplicities (Group "0..*" -- "0..*" User); verify the file starts with `@startuml`, ends with `@enduml` and names every class under `src/main/java`
