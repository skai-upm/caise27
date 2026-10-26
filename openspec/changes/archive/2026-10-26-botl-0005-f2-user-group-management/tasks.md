# Tasks

## 1. Group model and persistence

- [x] 1.1 Create `Group` (id, name, unmodifiable members view, add/remove/hasMember); verify with unit tests for duplicate membership and identity of members
- [x] 1.2 Create `GroupRepository` (`id;name;dni,dni` lines, DNI → User resolver, unknown DNIs skipped); verify with a `@TempDir` round-trip test that restored members are the same `User` instances returned by the resolver

## 2. Group rules and commands

- [x] 2.1 Create `GroupManager` (create, add, remove, delete, removeFromAllGroups; save after each change); verify with unit tests for every success and error case in the spec
- [x] 2.2 Make `UserManager.delete` return the deleted user and remove it from all groups in `CommandProcessor`; verify with a test that a deleted user disappears from groups, also after reload
- [x] 2.3 Add `GROUP CREATE/ADD/REMOVE/DELETE` to `CommandProcessor` and HELP; wire `GroupManager` with `data/groups.txt` in `App`; verify with command-level tests and a jar session with restart

## 3. Domain model

- [x] 3.1 Create `uml/model.puml` with `User`, `UserField`, `Group` and the `members` association with multiplicities; verify it passes `plantuml -checkonly`
- [x] 3.2 Run `mvn test` and verify all tests pass
