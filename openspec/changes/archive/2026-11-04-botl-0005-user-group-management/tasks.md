# Tasks

## 1. Group entity and persistence

- [x] 1.1 Create `Group` (final id, name, identity-based member list with add/remove/contains, unmodifiable view); verify with `GroupTest` (no duplicates, removal, identity semantics)
- [x] 1.2 Create `GroupFileRepository` (format `Id;Nombre;DNI...`, load with a DNI lookup, missing file = no groups, unknown DNIs and malformed lines skipped); verify with `GroupFileRepositoryTest` (exact line, round-trip with shared `User` instances, unknown DNI)

## 2. Group rules

- [x] 2.1 Create `GroupManager` (create/add/remove/delete/removeUserEverywhere, lookups through `UserManager`, save after success, rollback on save failure); make `UserManager.delete` return the deleted user; verify with `GroupManagerTest` (every scenario of the `user-groups` spec, restart recovery with same user instances, rejected commands leave the file unchanged)

## 3. Commands and wiring

- [x] 3.1 Dispatch `GROUP CREATE/ADD/REMOVE/DELETE` in `CommandProcessor` with parameter-count checks, call `removeUserEverywhere` after `USER DELETE`, add the four syntaxes to `HELP_LINES`, update constructors and `App.run` (sibling `groups.txt`), add `groups.txt` to `.gitignore`; verify with `CommandProcessorTest`/`AppTest` cases (appendix commands, wrong parameter counts, user deletion clears memberships across restart, HELP content) and the updated existing tests

## 4. Model and integration

- [x] 4.1 Create `docs/uml/model.puml` with all production classes, relationships and multiplicities; verify every class in `src/main/java` appears in it and the file starts with `@startuml` and ends with `@enduml`
- [x] 4.2 Run `mvn clean test` (all tests pass) and run the packaged JAR twice with group commands, confirming groups and memberships survive the restart
