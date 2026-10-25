# Tasks

## 1. Main controller and system settings

- [x] 1.1 Add `Validator.isValidId`, strict date parsing and price parsing helpers; verify unit tests for valid/invalid ids, dates (`2026-9-15`, `2026-02-30`, `15-09-2026`) and prices (`0`, `-5`, `abc`, `1.234`)
- [x] 1.2 Create `Park` (data directory, start date, price 40.00) owning `UserManager`; switch `CommandProcessor` and `App` to `Park` and add `SYSTEM DATE` / `SYSTEM PRICE` commands; verify existing tests still pass and new tests cover setting, moving backwards and rejecting date/price values

## 2. Groups

- [x] 2.1 Implement `Group` and `GroupRepository` (`groups.txt`, members by DNI, linked to loaded users); verify a unit test round-trips a group with members and skips malformed lines
- [x] 2.2 Implement `GROUP CREATE/ADD/REMOVE/DELETE` in `Park` and `CommandProcessor` with id, existence and duplicate checks; verify unit tests for each success and rejection, shared user object, and restoration after restart

## 3. Tickets

- [x] 3.1 Implement `Ticket`, `IndividualTicket`, `GroupTicket` (with `discountFor`) and verify unit tests for the date helpers and all discount brackets including 2/3, 5/6, 10/11 boundaries
- [x] 3.2 Implement `TicketRepository` and `GroupTicketRepository` with detached owners for history; verify round-trip tests including a ticket whose user no longer exists
- [x] 3.3 Implement `TICKET CREATE/DELETE` in `Park`/`CommandProcessor` with exact `OK TICKET CREATED -> <price>` reply; verify tests for unknown user, past date, duplicate id, delete today vs future, and `SYSTEM PRICE` effect
- [x] 3.4 Implement `GROUPTICKET CREATE/DELETE` with generated tickets, frozen composition and exact reply; verify tests for prices, empty group, id collisions and deletion of generated tickets
- [x] 3.5 Apply locks: `USER DELETE` rejected with tickets dated ≥ system date and removes the user from groups; group edits rejected while a group ticket is dated ≥ system date; verify tests for the spec sequences (2026-10-01 → blocked, 2026-10-16 → allowed) and history retention

## 4. Attractions

- [x] 4.1 Implement `Attraction` (atomic `enter`, `exit`, status) and `AttractionRepository` (no occupants persisted); verify unit tests for capacity, duplicates, atomic group rejection and restart with empty attraction
- [x] 4.2 Implement `ATTRACTION CREATE/DELETE/ENTER/EXIT/STATUS` in `Park`/`CommandProcessor` with system-date validity on entry; verify tests for individual and group entry, wrong date, full capacity, exit freeing places and status contents

## 5. Command interface and fly mode

- [x] 5.1 Update `HELP` with every new command; verify the HELP test checks all listed syntaxes
- [x] 5.2 Implement `--fly` in `App` (timestamped `tmp/` directory, same repositories, cleanup in `finally` and shutdown hook) and add `tmp/` to `.gitignore`; verify tests that a fly run persists inside the temp dir only, deletes it after `EXIT`/end of input, and works with a command file

## 6. UML documentation

- [x] 6.1 Write `uml/model.puml` from the final code following the design conventions; verify it renders with the PlantUML jar without errors and that classes, fields and relationships match the source

## 7. Integration check

- [x] 7.1 Run `mvn test`, `mvn package`, then run the JAR with `--fly` on a scenario file covering date changes, groups, tickets, group tickets, attractions and deletion locks, and confirm the normal data files are untouched and the temp directory is removed
