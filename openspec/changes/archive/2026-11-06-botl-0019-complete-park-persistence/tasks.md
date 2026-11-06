# Tasks

## 1. Persistence components

- [x] 1.1 Add `TextFile`, `GroupFileStore`, `AttractionFileStore` and `TicketFileStore` (formats in design.md, snapshots for deleted users, detached group tickets) and verify with round-trip unit tests including a deleted user and a detached group ticket
- [x] 1.2 Add `DataDirectory` (permanent/temporary, timestamped, recursive delete on close) and verify with unit tests

## 2. Repositories and services

- [x] 2.1 Back `GroupRepository`, `AttractionRepository` and `TicketRepository` with optional stores (load on construction, `save()`), call `save()` after successful mutations in the services and `ParkController`, and verify with unit tests that each mutation is persisted
- [x] 2.2 Add `ParkController.start(Path, LocalDate)` reconstructing users → groups → attractions → tickets, and verify with a restart test: groups, tickets (links, prices, composition), locked group, deleted user/group fiscal records, empty attractions

## 3. Execution modes

- [x] 3.1 Make `Main` accept `[--fly] [commandFile]` using `DataDirectory`, update `.gitignore`, and verify with `MainTest` that `--fly` with file and interactive (`EXIT` and end of input) leaves permanent files untouched and deletes the temporary directory, and that normal mode is unchanged

## 4. Integration

- [x] 4.1 Run `mvn package` and verify all tests pass; run the JAR in a temp dir: normal run creating all element types, restart and inspect; then `--fly` with a file and verify the permanent files are unchanged and `tmp/` holds no leftover directory
- [x] 4.2 Run `openspec validate botl-0019-complete-park-persistence --strict` and verify the change is valid
