# Tasks

## 1. Group model and persistence

- [x] 1.1 Create `Group` (id, name, member references) with duplicate-safe add/remove; verify unit tests for add, duplicate add and remove
- [x] 1.2 Create `GroupRepository` saving `Id;Nombre;DNI,...` lines and relinking members through a DNI lookup on load; verify a round-trip test in a temp directory that restores member references

## 2. Group management and commands

- [x] 2.1 Create `GroupManager` with create/delete/add/remove rules (unique id, existing user, no duplicate member) and `removeUserFromAllGroups`; verify unit tests for every rule and for restore after restart
- [x] 2.2 Add `GROUP CREATE/ADD/REMOVE/DELETE` to `CommandProcessor` and HELP, wire `GroupManager` in `App` with `data/groups.txt`; verify command tests including parameter-count errors and HELP content
- [x] 2.3 Make `USER DELETE` remove the user from all groups; verify a test where the group no longer contains the user after deletion and after reload
- [x] 2.4 Verify a group member reflects a user update (same object), and run `mvn test` to verify the full suite passes

## 3. Model documentation

- [x] 3.1 Create `uml/model.puml` with current classes, the `Group` → `User` association (`0..*`) and managers/repositories; verify the file starts with `@startuml` and ends with `@enduml` and names every class in `src/main/java`
