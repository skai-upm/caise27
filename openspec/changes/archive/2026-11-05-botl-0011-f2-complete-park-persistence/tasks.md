# Tasks

## 1. Domain support

- [x] 1.1 Add `ParkController.restoreGroup/restoreIndividualTicket/restoreGroupTicket` (with historical group linking) and a `GroupTicket` constructor that accepts a missing group; verify unit tests

## 2. Repositories

- [x] 2.1 Implement `TextFile`, `GroupRepository`, `AttractionRepository`, `TicketRepository` and `ParkStorage`; verify round-trip tests covering relationships, deleted-group tickets, reused group ids, empty attractions, missing files and corrupted lines

## 3. Application wiring

- [x] 3.1 Add the after-command hook to `CommandProcessor`, load at startup and save after each command in `App`, and parse `[--fly] [file]` with temp-directory creation and deletion; verify `App` tests for restart persistence and all four argument forms, including cleanup with and without `EXIT`
- [x] 3.2 Ignore `data/` and `tmp/` in `.gitignore`; verify `git status` does not list them after a manual run

## 4. Verification

- [x] 4.1 Run `mvn -q package` and then the JAR with `--fly input.txt` and in normal mode; verify all tests pass, the temp directory is removed, and data persists between normal runs
