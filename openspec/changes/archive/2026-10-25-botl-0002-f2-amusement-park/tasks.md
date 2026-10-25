# Tasks

## 1. Main controller and system date

- [x] 1.1 Add `ParkController` (system date initialised once from the OS clock, ticket price 40.00, built from a data directory) and route `CommandProcessor` through it, keeping all USER commands working; verify existing tests pass after adapting construction
- [x] 1.2 Implement `SYSTEM DATE;yyyy-MM-dd` with strict ISO parsing; verify unit tests for valid date, backward change and rejected formats (`15/09/2026`, `2026-9-15`, `2026-02-30`)
- [x] 1.3 Add `Identifiers.isValid` (letters, digits, `-`, `_`; no regex); verify with unit tests

## 2. Groups

- [x] 2.1 Implement `Group`, `GroupRepository` (`id;name;dni,...`, resolving members by DNI) and `GroupManager` (create, add, remove, delete, remove user from all groups); verify unit tests for each rule (unknown user, duplicate member, not a member, unknown/duplicate group, invalid id) and round-trip persistence linking the same `User` objects
- [x] 2.2 Wire `GROUP CREATE/ADD/REMOVE/DELETE` commands through `ParkController`; verify with command-level tests including that a member reflects a later `USER UPDATE`

## 3. Individual tickets

- [x] 3.1 Implement abstract `Ticket` and `IndividualTicket` with date predicates (`isValidOn`, `isCurrentOrFuture`, `isRefundable`); verify unit tests for each boundary (same day, day before, day after)
- [x] 3.2 Implement `TicketManager` and `TicketRepository` for individual tickets (`id;date;dni;price;groupTicketId`), keeping tickets of deleted users with their DNI; verify round-trip and history unit tests
- [x] 3.3 Implement `TICKET CREATE` (existing user, unique id, date ≥ system date, current price, `OK TICKET CREATED -> <price>`), `TICKET DELETE` (date > system date only) and `SYSTEM PRICE`; verify command-level tests for the spec scenarios
- [x] 3.4 Change user deletion: authenticate, reject when any ticket dated ≥ system date exists, remove the user from groups, then delete; verify tests for blocked, unblocked-after-date-change and group removal scenarios

## 4. Group tickets

- [x] 4.1 Implement `GroupTicket` (composition of internal `IndividualTicket`s with back-reference, group aggregation, `discountFor`) and creation through `TicketManager`; verify tests for all brackets and boundaries (2/3, 5/6, 10/11 members) and the 108.00 example
- [x] 4.2 Implement group locking (no add/remove/delete while a group ticket is ≥ system date) and internal-ticket blocking of user deletion; verify tests including lock release after advancing the date
- [x] 4.3 Implement `GROUPTICKET DELETE` (date > system date, removes internal tickets) and reject `TICKET DELETE` on internal tickets; verify tests
- [x] 4.4 Persist group tickets (`grouptickets.txt` + internal tickets linked by `groupTicketId`); verify restart test restores TG01 with its tickets and the group lock

## 5. Attractions

- [x] 5.1 Implement `Attraction` (capacity, present users) with polymorphic `enter(Ticket, date)` (date check, atomic capacity check, no duplicate presence) and `exit(Ticket)`; verify unit tests for individual, group, wrong date, insufficient room and exit
- [x] 5.2 Implement `AttractionManager`/`AttractionRepository` (`id;name;capacity`, empty on load) and commands `ATTRACTION CREATE/DELETE/ENTER/EXIT/STATUS`; verify command-level tests including the STATUS example after entries and exit, and restart with empty attractions

## 6. Execution modes and help

- [x] 6.1 Implement argument parsing (`--fly`, single file path, errors for unknown options/extra paths) and the temporary `tmp/<yyyyMMdd_HHmmss>` data directory with recursive deletion in `finally` plus a shutdown hook; verify tests that a fly run leaves permanent data untouched and removes its directory after EXIT and after end of input
- [x] 6.2 Update `HELP` with every new command; add `tmp/` to `.gitignore`; verify the help test lists all commands from the spec

## 7. Design model

- [x] 7.1 Write `uml/model.puml` from the final code following the design-model notation rules; verify it with PlantUML `-checkonly` and by comparing classes, attributes and relationships against the source

## 8. Integration

- [x] 8.1 Run `mvn test`; build `target/parque.jar` and run an end-to-end command file covering SYSTEM DATE, groups, tickets, group tickets and attractions in normal and `--fly` modes; verify outputs match the spec examples and `tmp/` is empty afterwards
