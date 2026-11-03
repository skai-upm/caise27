# Tasks

## 1. Group model and service

- [x] 1.1 Create `Group` (final id, name, member references, identity-based contains) and verify `GroupTest` (add/remove/contains, unmodifiable members view)
- [x] 1.2 Create `GroupService` (create, addMember, removeMember, delete, removeUserFromAll) with uniqueness, registered-user and duplicate-member checks; verify `GroupServiceTest` covers every rule, nonexistent group/user cases and that members are the same objects held by `UserService`

## 2. Persistence

- [x] 2.1 Create `GroupFileStorage` (`Id;Nombre;DNI,DNI` lines, load linking DNIs to loaded users, unknown DNIs skipped, missing file → empty) and save after each successful change with revert on failure; verify `GroupFileStorageTest` exact line format, round trip and identity of restored members

## 3. Commands and wiring

- [x] 3.1 Make `UserService.delete` return the removed user and remove it from all groups on USER DELETE; verify a test that deleting Ana empties her memberships and updates `groups.txt`
- [x] 3.2 Add `GROUP CREATE/ADD/REMOVE/DELETE` to `CommandProcessor` (parameter counts, OK/ERROR responses) and to HELP; wire `GroupService` in `App` with a data directory holding `users.txt` and `groups.txt`; add `groups.txt` to `.gitignore`; verify `CommandProcessorTest`/`AppTest` cover the four commands, malformed commands and the ten HELP entries

## 4. UML

- [x] 4.1 Create `uml/model.puml` with current classes, relationships and multiplicities; verify it passes `java -jar plantuml.jar -checkonly uml/model.puml`

## 5. Integration verification

- [x] 5.1 Verify restart: an `AppTest` creates users and groups, adds/removes members, restarts over the same data directory and checks the restored memberships; then `mvn clean test` passes
