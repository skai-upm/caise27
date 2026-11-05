# Tasks

## 1. File store

- [x] 1.1 Create `persistence.ParkFileStore` (groups, tickets, attractions text formats; load linking by DNI/id with detached historical holders and nullable group; malformed-line counting; save) and verify `ParkFileStoreTest`: round trip of groups/members, individual and group tickets with composition and group, attractions restored empty, deleted-user ticket restored with holder data, deleted-group ticket restored with null group, malformed lines counted

## 2. Integration with the park

- [x] 2.1 Add `ParkController.startExecution(UserRepository, ParkFileStore)` and `persist()`; make `CommandProcessor` persist after OK responses; verify a test that commands are saved and a new execution restores them and still enforces locks/identifier uniqueness

## 3. Fly mode and entry point

- [x] 3.1 Add `StorageLocation` (normal vs `--fly` timestamped tmp dir with collision suffix) and update `App` to strip `--fly`, use the chosen directory for every repository, report ignored lines, and delete the fly directory in `finally`; add `tmp/` to `.gitignore`; verify `AppTest` for `--fly` with file and with stdin+EXIT (normal files untouched, tmp removed) and normal mode unchanged

## 4. Integration

- [x] 4.1 End-to-end with the JAR in a temp directory: normal run creating users/group/tickets/attraction, restart and verify restored state (attraction empty, lock still active); `--fly input.txt` and `--fly` interactive leave normal files untouched and no `tmp/<ts>` left; full `mvn package` regression
