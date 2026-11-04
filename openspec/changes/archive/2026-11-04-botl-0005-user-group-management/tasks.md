# Tasks

## 1. Model and in-place user updates

- [x] 1.1 Create `Group` (id, name, member list by reference, no duplicates); verify `GroupTest` for add/remove/contains and unmodifiable members view
- [x] 1.2 Change `UserService.update` to modify the stored `User` in place after a successful save, and make `delete` return the removed `User`; verify existing `UserServiceTest` still passes and a new test asserts the same instance is kept after `update`

## 2. Group persistence

- [x] 2.1 Implement `GroupRepository` (`Identificador;Nombre;DNI,DNI` lines, DNI resolution through a lookup function, skip unknown DNIs/malformed lines, missing file → empty); verify `GroupRepositoryTest` round-trip, empty member list and unknown DNI

## 3. Group operations and commands

- [x] 3.1 Implement `GroupService` (create, add, remove, delete, removeUserFromAllGroups) with atomic save; verify `GroupServiceTest` for every rule in the `user-groups` spec and unchanged file on rejection
- [x] 3.2 Wire `GROUP CREATE/ADD/REMOVE/DELETE` and the four HELP lines into `CommandProcessor`, cascade on `USER DELETE`, and make `App.run` take a data directory with `users.txt` and `groups.txt`; verify `CommandProcessorTest`/`AppTest` updated and passing, including the cascade scenario

## 4. UML model

- [x] 4.1 Create `uml/model.puml` with all current classes, the `Group`–`User` association and multiplicities; verify `UmlModelTest` that every class in `src/main/java` is declared and the file is enclosed in `@startuml`/`@enduml`

## 5. Integration verification

- [x] 5.1 Verify restart restores groups with shared user instances and that an email change survives restart (`AppTest` with two runs on the same data directory), and that `mvn test` and `mvn package` succeed
