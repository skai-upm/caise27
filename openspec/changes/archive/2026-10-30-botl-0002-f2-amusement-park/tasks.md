# Tasks

Each group keeps `mvn test` green and preserves F1 behavior.

## 1. Baseline and system settings

- [x] 1.1 Review F1 baseline (run `mvn test`, confirm 63 passing tests) and add `Park` controller holding system date (OS date at start) and base price 40.00, wired into `CommandProcessor`; verify existing tests still pass
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` (strict ISO parsing) and `SYSTEM PRICE;<price>` (positive, ≤ 2 decimals); verify with `ParkSettingsTest` (valid/invalid dates incl. 2026-02-30, backward moves, invalid prices, unchanged value on error)

## 2. Groups

- [x] 2.1 Implement `Group`, `GroupRepository` (`groups.txt`, members by DNI) and `GroupService` (create, add, remove, delete, removeUserFromAll); verify with `GroupServiceTest` (unknown user, duplicate member, non-member removal, duplicate id, restart restores member references)
- [x] 2.2 Add `GROUP CREATE/ADD/REMOVE/DELETE` commands through `Park`; verify with command-level tests

## 3. Individual tickets

- [x] 3.1 Implement abstract `Ticket` and `IndividualTicket`, `TicketService` and `TicketRepository` (`tickets.txt` with user snapshot, detached historical users); verify with `TicketServiceTest` (create, price fixed after `SYSTEM PRICE`, unique ids, past date rejected, restart restores, historical ticket of deleted user)
- [x] 3.2 Add `TICKET CREATE` (exact `OK TICKET CREATED -> <Precio>`) and `TICKET DELETE` (strictly future only) commands; verify past/today/future deletion test
- [x] 3.3 Modify user deletion in `Park` to reject users with tickets dated ≥ system date and remove deleted users from groups; verify the spec scenarios (blocked on 2026-10-01, allowed on 2026-10-16, history kept)

## 4. Group tickets

- [x] 4.1 Implement `GroupTicket` (group aggregation, constituent composition, discount pricing) and its persistence in `grouptickets.txt`; verify with `GroupTicketTest` discount boundaries 2/3, 5/6, 10/11 (80, 108, 180, 192, 320, 220 at base 40) and restart restoring constituents
- [x] 4.2 Add `GROUPTICKET CREATE` (exact `OK GROUP TICKET CREATED -> <Precio>`, atomic, id collisions rejected) and `GROUPTICKET DELETE` (future only, all constituents); verify all-or-nothing tests
- [x] 4.3 Enforce group locking (add/remove/delete rejected while a group ticket dated ≥ system date exists) and constituent users' deletion block; verify lock and unlock-after-date tests

## 5. Attractions

- [x] 5.1 Implement `Attraction`, `AttractionRepository` (`attractions.txt`, definitions only) and `AttractionService`; add `ATTRACTION CREATE`; verify invalid capacity/duplicate id and restart-empty tests
- [x] 5.2 Add `ATTRACTION ENTER` (ticket valid on system date, capacity, atomic group entry, already-present rejection), `ATTRACTION EXIT` (ticket id, no date check) and `ATTRACTION STATUS`; verify with `AttractionTest` (atomic capacity rejection, entry/exit/status counts, exit after date change)

## 6. Command execution and temporary persistence

- [x] 6.1 Update `HELP` with all F2 commands; verify help test lists every command
- [x] 6.2 Add `StorageLocation` and `--fly` argument parsing in `App` (flag anywhere, first other argument is the file), routing all repositories to `tmp/<yyyyMMdd_HHmmss>/` and deleting it on termination (finally + shutdown hook); verify with `FlyModeTest` (permanent files unchanged, tmp dir removed after EXIT and end of file, same output as normal mode, interactive and file variants)
- [x] 6.3 Add `tmp/` and the new data files to `.gitignore` and update README with F2 commands and `--fly`; verify the documented commands run

## 7. UML and final verification

- [x] 7.1 Write `uml/model.puml` from the final code following the presentation rules in design.md; verify it renders with PlantUML (`java -jar plantuml.jar -checkonly`) and cross-check each relationship against the source fields
- [x] 7.2 Run `mvn clean package`, then execute an F2 command file end-to-end in normal mode (restart persistence of groups/tickets/attractions, empty attractions on restart) and with `--fly` (no changes to permanent files); verify outputs and that all F1 tests still pass
