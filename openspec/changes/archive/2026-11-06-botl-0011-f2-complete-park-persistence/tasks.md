# Tasks

## 1. Storage port and model support

- [x] 1.1 Add `ParkState`, the `ParkStorage` port (default no-op), `ParkController.restore` and a save after every successful mutation, plus `GroupTicket.restore` for rebuilding compositions, and verify with a controller test (in-memory recording storage) that each mutation saves and that a rejected operation does not

## 2. Text repositories

- [x] 2.1 Add `StoragePaths` and the group, ticket and attraction repositories with the documented line formats, atomic writes and tolerant loading, and verify with `TextParkStorageTest` (@TempDir) a full round-trip of groups, individual tickets, group tickets with and without group reference, and attractions (empty occupants)
- [x] 2.2 Verify with tests that a malformed line is reported and skipped, that missing files load as empty, and that a historical group ticket of a deleted group stays unlinked after restart even when the group id is reused

## 3. Execution modes

- [x] 3.1 Update `App` for the four argument forms (`--fly` with a timestamped `tmp/` directory, wiring, restore, cleanup in `finally`) using a configurable base directory, and verify with `AppTest` that a normal run persists across two runs, that a `--fly` run neither sees nor changes normal data, and that the temporary directory is deleted after EXIT and after end of input
- [x] 3.2 Add `data/` and `tmp/` to `.gitignore`, document persistence and `--fly` in `README.md`, and verify that `mvn clean package` passes and that `java -jar target/amusement-park.jar --fly <file>` leaves no `tmp/<timestamp>` directory
