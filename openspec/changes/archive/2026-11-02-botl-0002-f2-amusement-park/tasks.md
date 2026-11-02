# Tasks

## 1. F1 baseline and shared infrastructure

- [x] 1.1 Review the F1 baseline and confirm `mvn test` passes before changes (65 tests)
- [x] 1.2 Add `LineFile` (shared read/write of `;`-separated lines) and `StoragePaths`; refactor `UserFileStorage` to use them without changing the `users.txt` format; verify the existing storage tests still pass
- [x] 1.3 Add `ParkController` holding the system date and base price, route `CommandProcessor` through it, and keep all F1 command behavior; verify the F1 test suite passes unchanged in behavior

## 2. System date and base price

- [x] 2.1 Implement `SYSTEM DATE` (strict `uuuu-MM-dd`, forward/backward, invalid rejected without change) and `SYSTEM PRICE` (positive, ≤2 decimals); verify with unit tests for valid, invalid, and default values

## 3. Groups

- [x] 3.1 Implement `Group`, `GroupFileStorage`, and `GroupManager` with `GROUP CREATE/ADD/REMOVE/DELETE`, references to existing users, duplicate/unknown rejection, and persistence by DNI; verify with tests including restart restoring the same user objects

## 4. Individual tickets

- [x] 4.1 Implement the `Ticket` base class and `IndividualTicket`, `TicketFileStorage` with holder snapshot, and `TICKET CREATE` (`OK TICKET CREATED -> <Precio>`, unique id, existing user, date ≥ system date); verify with tests for success, errors, and recorded price after `SYSTEM PRICE`
- [x] 4.2 Implement `TICKET DELETE` (strictly future only, constituents rejected) and the `USER DELETE` block for tickets dated on or after the system date, plus removal of deleted users from groups; verify with tests for past/today/future tickets, deletion after the date passes, and history surviving restart after user deletion

## 5. Group tickets

- [x] 5.1 Implement `GroupTicket` (composition of constituents `<id>-<n>`), `group_tickets.txt` storage, `GROUPTICKET CREATE` with discount brackets and all-or-nothing creation; verify with tests for 2/3/5/6/10/11 members, failures leaving no tickets, and restart restoring composition
- [x] 5.2 Implement group locking (group ticket dated ≥ system date blocks `GROUP ADD/REMOVE/DELETE`), member deletion block, and `GROUPTICKET DELETE` (future only, deletes all constituents); verify with tests for locked/unlocked transitions and atomic deletion

## 6. Attractions

- [x] 6.1 Implement `Attraction`, `AttractionFileStorage`, and `ATTRACTION CREATE/ENTER/EXIT/STATUS` using the `Ticket` abstraction (date equals system date, capacity, atomic group entry, exit by ticket or email without date check, runtime-only occupancy); verify with tests for capacity rejection, status before/after exit, exit after date change, and empty attractions after restart

## 7. Commands, help, and --fly

- [x] 7.1 Update `HELP` to list every F1 and F2 command and verify with a test that each syntax line is present
- [x] 7.2 Implement `--fly` in `App` (timestamped `tmp/` directory, all repositories via `StoragePaths`, recursive deletion in `finally` and a shutdown hook, interactive and file modes), set `finalName` to `parque`, ignore `tmp/` in git; verify with tests that permanent files are untouched, outputs match normal mode, and the directory is removed after `EXIT`

## 8. UML

- [x] 8.1 Write `uml/model.puml` from the implemented code following the presentation rules recorded in design D4, and verify it is accepted by PlantUML (`-checkonly`) and that each relationship corresponds to a field in the code

## 9. Validation and regression

- [x] 9.1 Run `mvn test` and `mvn package`, then execute `target/parque.jar` with a sample F2 command file in normal mode (twice, to check restart) and in `--fly` mode, confirming permanent files are unchanged by `--fly` and no `tmp/` run directory remains
