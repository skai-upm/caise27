# Tasks

## 1. Storage of new elements

- [x] 1.1 Create `GroupFileStorage`, `AttractionFileStorage` and `TicketFileStorage` (formats from design, DNI linking, detached users for deleted ones, group ticket assembly, malformed lines skipped) and verify with `ParkStorageTest` round trips for groups, attractions, individual and group tickets, deleted-user and deleted-group historical tickets
- [x] 1.2 Give `GroupRepository`, `TicketRepository` and `AttractionRepository` optional storages (load at construction, save on change, `persist()`), extend `ParkController.initialize`/`persistAll`, and persist after every command in `CommandProcessor`; verify with existing tests plus repository reload tests

## 2. Application wiring and temporary mode

- [x] 2.1 Build all repositories from the data directory in `App` and add `--fly` (timestamped `tmp/` directory, cleanup on EXIT/end of input, Delivery 1 argument handling otherwise); verify with `PersistenceIntegrationTest`: full state after restart, locks after restart, empty attractions after restart, fly isolation and cleanup for file and interactive modes
- [x] 2.2 Run `mvn package` and verify the four execution forms with the JAR (normal data written; `--fly` leaves no `tmp/<timestamp>` directory and does not touch `data/`)
