# Tasks

## 1. F2-C01 — Park controller and system date

- [x] 1.1 Introduce the `ParkController` singleton (`getInstance`, `initialize(ParkRepositories, LocalDate)`), move `UserService` user operations into it, add `ParkRepositories` (base-directory path configuration) and update `App`/`CommandProcessor`; verify the Delivery-1 tests pass after migrating `UserServiceTest` to `ParkControllerTest`
- [x] 1.2 Add `SYSTEM DATE` and `SYSTEM PRICE` with validation (ISO date; positive amount, max two decimals); verify tests for date change, invalid date/price, singleton identity, and price reset on re-initialisation

## 2. F2-C02 — User groups

- [x] 2.1 Implement `Group` (identifier, name, member `User` list) and `GROUP CREATE`/`GROUP ADD`/`GROUP REMOVE` with uniqueness, existing-user and computed-lock checks; verify tests for creation, duplicates, unknown user/group, duplicate member and removal

## 3. F2-C03 — Individual tickets

- [x] 3.1 Implement abstract `Ticket` and `IndividualTicket`, the shared identifier lookup, `TICKET CREATE` (shows price) and `TICKET DELETE` (strictly future, standalone only), and extend `USER DELETE` to reject users with active tickets; verify tests for past/today/future dates, price snapshot after `SYSTEM PRICE`, duplicate identifier and deletion rules

## 4. F2-C04 — Group tickets

- [x] 4.1 Implement `GroupTicket` (nullable group, fixed composition, discount brackets, price = sum), `GROUPTICKET CREATE` (atomic on any identifier conflict) and `GROUPTICKET DELETE`, `GROUP DELETE`, group locks, and `USER DELETE` checks for locked groups and group removal; verify tests for 2/3/6/11 member totals, derived-identifier conflict, empty group, lock/unlock, historical tickets after group/user deletion

## 5. F2-C05 — Attractions

- [x] 5.1 Implement `Attraction` (identifier, name, capacity, occupant list), `ATTRACTION CREATE` and read-only `ATTRACTION LIST`; verify tests for creation, invalid capacity, duplicates, empty and occupied listings

## 6. F2-C06 — Individual attraction access

- [x] 6.1 Implement `ATTRACTION ENTER` through the `Ticket` abstraction for individual tickets (date = today, capacity, holder not inside, holder registered); verify tests for valid entry, wrong date, full attraction and presence surviving a date change

## 7. F2-C07 — Group attraction access

- [x] 7.1 Extend entry to group tickets (all-or-nothing), add `ATTRACTION EXIT` and `ATTRACTION STATUS`; verify tests for a fitting and a non-fitting group, individual exit of a group member, unknown occupant, and status contents

## 8. F2-C08 — Park persistence and fly mode

- [x] 8.1 Implement `GroupRepository`, `TicketRepository` and `AttractionRepository` text formats, loading with relationship re-linking (detached holders for deleted users, null group for deleted groups) and save-all commit with undo on failure; verify round-trip tests including historical tickets and empty occupants after reload
- [x] 8.2 Implement `--fly` (first argument, optional command file, timestamped `tmp/` directory, same repositories, recursive cleanup after EXIT/end of input); verify `AppTest` cases for `--fly` with file and interactive, and that permanent files are untouched

## 9. F2-C09 — PlantUML model

- [x] 9.1 Write `uml/model.puml` matching the domain classes and fields; verify it parses with the cached PlantUML jar (`-checkonly`) and review each relationship against the code

## 10. F2-C10 — Delivery 2 integration

- [x] 10.1 Extend `HELP` with all Delivery 1 and 2 commands and add an end-to-end mixed Phase 1/2 command-file test; verify the HELP test and the mixed-file invariants
- [x] 10.2 Run `mvn clean package` and run the JAR in interactive, file, `--fly` and `--fly <file>` modes from a temporary directory; verify outputs, cleanup, and absence of regex/serialization/runtime dependencies
