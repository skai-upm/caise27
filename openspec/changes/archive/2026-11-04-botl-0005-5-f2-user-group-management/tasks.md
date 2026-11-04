# Tasks

## 1. Shared user references

- [x] 1.1 Add `findByEmail`/`findByDni` to `UserManager` and change `update` to mutate the stored instance only after a successful save; verify with `UserManagerTest` that the instance identity is preserved after updates and existing tests still pass

## 2. Group model and operations

- [x] 2.1 Implement `Group` (id, name, member references, copy constructor sharing users); verify with `GroupTest` (add/remove/contains by identity, unmodifiable members, copy shares users)
- [x] 2.2 Implement `GroupFileStore` (format `id;name;dni...`, missing file, unknown DNI and malformed lines skipped); verify with `GroupFileStoreTest` (exact line, round trip resolves the same `User` instances, skips)
- [x] 2.3 Implement `GroupManager` create/add/remove/delete/removeUserEverywhere with save-then-swap atomicity; verify with `GroupManagerTest` (all acceptance scenarios, file unchanged on rejection, restart restores relationships with shared instances)

## 3. Commands and wiring

- [x] 3.1 Dispatch `GROUP CREATE|ADD|REMOVE|DELETE` in `CommandProcessor`, remove deleted users from groups, add the four syntaxes to HELP, and wire `groups.txt` in `App`; verify with `CommandProcessorTest`/`AppTest` (source example commands, malformed group commands, email update visible through group, user delete cascade, restart via `App.run`)

## 4. UML model and regression

- [x] 4.1 Create `uml/model.puml` documenting current classes, relationships and multiplicities; verify it parses with PlantUML `-checkonly`
- [x] 4.2 Verify `mvn test` passes with all previous tests and a packaged-JAR run of the source's group commands across two executions restores the group
