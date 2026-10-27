# Tasks

## 1. Stores and repositories

- [x] 1.1 Add `DataPaths`, `TextFiles` and `AttractionFileStore`, `GroupFileStore`, `TicketFileStore` (formats per design, snapshot users for deleted users); verify round trips in `ParkFileStoresTest`
- [x] 1.2 Give `GroupRepository`, `TicketRepository`, `AttractionRepository` optional stores with load-on-create and `save()`; add `ParkController.start(DataPaths)` loading in order and persisting membership/group-reference changes; verify restart scenarios in `ParkPersistenceTest` (groups, tickets, historical records, attractions empty)

## 2. Temporary mode

- [x] 2.1 Handle `--fly` / `--fly <file>` in `Main` with timestamped `tmp/` directory deleted at the end; verify in `FlyModeTest` (file, interactive with EXIT, isolation from permanent data, identical repeated runs) and with a manual `java -jar target/nombre-aplicacion.jar --fly input.txt`
- [x] 2.2 Verify no serialization APIs in production sources via `TechnicalRestrictionsTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F2-C08] Extend park persistence`; verify with `git log -1 --format=%s`
