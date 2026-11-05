# Tasks

## 1. Storages

- [x] 1.1 Add `GroupStorage`/`GroupFileStorage`, `TicketStorage`/`TicketFileStorage`, `AttractionStorage`/`AttractionFileStorage` with the documented formats and wire them into the repositories (save after each mutation); verify round-trip unit tests including composition, group reference, deleted user and detached group
- [x] 1.2 Make services persist mutations (`GroupService` membership/detach, `UserService.deleteUser` re-saving tickets); verify a restart test of the full scenario including rules still enforced and empty attractions

## 2. Park start-up and fly mode

- [x] 2.1 Add `Park.start(Path dataDirectory)` loading all repositories in dependency order; change `App.run` to take the data directory; verify existing tests pass
- [x] 2.2 Implement `--fly [file]` creating `tmp/<yyyyMMdd_HHmmss>`, using it as data directory and deleting it at the end; verify tests that permanent files are untouched and the directory is removed in interactive and file mode

## 3. Integration

- [x] 3.1 Run the JAR in the four argument forms in a temp working directory and verify persistence/restart behavior and `--fly` cleanup; run `mvn clean package` and verify all tests pass
