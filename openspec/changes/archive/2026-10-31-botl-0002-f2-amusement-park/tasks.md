# Tasks

## 1. Baseline and storage foundation

- [x] 1.1 Review the F1 baseline and run `mvn test`; verify all existing tests pass before changes
- [x] 1.2 Add `TextFileStore` and `StoragePaths`, refactor `UserRepository` onto `TextFileStore` without changing the `users.txt` format; verify existing `UserRepositoryTest` still passes
- [x] 1.3 Add `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt` and `tmp/` to `.gitignore`; verify with `git status` after a run

## 2. System date, price and controller

- [x] 2.1 Add `ParkController` holding the system date and base price and owning the managers; route `USER CREATE/UPDATE/DELETE` through it; verify F1 command tests still pass
- [x] 2.2 Implement `SYSTEM DATE` (strict `uuuu-MM-dd`) and `SYSTEM PRICE` (non-negative decimal) commands; verify with tests for valid, backward, malformed and non-existent dates and invalid prices

## 3. Groups

- [x] 3.1 Implement `Group`, `GroupRepository` and `GroupManager` with `GROUP CREATE/ADD/REMOVE/DELETE`; verify with tests for unknown users, duplicate members, duplicate ids, reference identity and restart restoration

## 4. Individual tickets

- [x] 4.1 Implement `Ticket`, `IndividualTicket`, `TicketRepository` (with holder snapshot) and `TicketManager` with `TICKET CREATE` (exact `OK TICKET CREATED -> <Precio>`) and `TICKET DELETE`; verify with tests for unknown user, duplicate id, past date, price fixed after `SYSTEM PRICE`, refund before/equal/after the system date, and restart
- [x] 4.2 Extend user deletion: block on tickets dated ≥ system date, remove the user from groups and attractions, keep historical tickets; verify with the spec scenarios including restart with a deleted holder

## 5. Group tickets

- [x] 5.1 Implement `GroupTicket` (aggregation to group, composition of constituents `<id>-<n>`), pricing brackets and `GROUPTICKET CREATE/DELETE`, persistence and restoration; verify with tests for 2/3, 5/6, 10/11 boundaries, atomic failure, composition captured at purchase, and all-or-nothing deletion
- [x] 5.2 Enforce group locking (group tickets dated ≥ system date) on `GROUP ADD/REMOVE/DELETE` and constituent-based user deletion block; verify locked and released cases by moving the system date

## 6. Attractions

- [x] 6.1 Implement `Attraction`, `AttractionRepository` and `AttractionManager` with `ATTRACTION CREATE/ENTER/EXIT/STATUS` using `Ticket.getHolders()` polymorphically; verify with tests for individual and group entry, wrong-date ticket, already-present user, atomic capacity rejection, exit after a date change, status contents, and empty attractions after restart

## 7. Execution modes and help

- [x] 7.1 Update `HELP` with all F2 commands; verify with a test listing every command
- [x] 7.2 Implement `--fly` in `App` (timestamped `tmp/` directory, all repositories redirected, deletion in `finally`) for interactive and file modes; verify with tests that permanent files are untouched, the temp directory is removed after `EXIT`, end of input and an unreadable file, and outputs match a normal run

## 8. UML documentation

- [x] 8.1 Write `uml/model.puml` from the implemented fields following the presentation rules in design D5; verify PlantUML (`net.sourceforge.plantuml` 1.2024.7, `-checkonly`) reports no errors and every relationship matches a declared field

## 9. Integration verification

- [x] 9.1 Run `mvn test` and `mvn package`; run the packaged JAR with an F2 command file normally and with `--fly`, confirming outputs, discount prices, locking, attraction status, restart restoration, and that `--fly` leaves permanent files unchanged
