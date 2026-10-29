# Tasks

## 1. Park controller, date and price (F2-C01)

- [x] 1.1 Implement `ParkController` singleton (`getInstance`, `initialize(dataDir)`, current date, price 40.00) owning `UserService`; route existing USER commands through it; verify `ParkControllerTest` (same instance, initial date/price) and all Delivery 1 tests still pass
- [x] 1.2 Add `SYSTEM DATE` and `SYSTEM PRICE` parsing/validation and commands; verify tests for valid/invalid dates and prices and price reset on `initialize`

## 2. Groups (F2-C02)

- [x] 2.1 Implement `Group` and `GROUP CREATE`/`GROUP ADD`/`GROUP REMOVE` with duplicate checks and derived lock; verify `GroupTest` cases (create, duplicate id, duplicate member, unknown user/group, remove non-member)

## 3. Tickets (F2-C03, F2-C04)

- [x] 3.1 Implement `Ticket`/`IndividualTicket`, shared id space, `TICKET CREATE` (price printed, past date rejected) and `TICKET DELETE` (strictly future only, composition tickets protected); verify `TicketTest`
- [x] 3.2 Implement `GroupTicket` with composition `<id>:<dni>`, discount brackets, atomic id-conflict check, `GROUPTICKET CREATE`/`GROUPTICKET DELETE`; verify tests for 6×40 → 32/192, bracket boundaries, empty group, conflict without partial creation, deletion rules
- [x] 3.3 Implement `GROUP DELETE` and lock checks on `GROUP ADD`/`GROUP REMOVE` (active vs historical, re-unlock after `GROUPTICKET DELETE`) and null group reference on historical tickets; verify tests
- [x] 3.4 Extend `USER DELETE` (active individual/composition tickets, locked groups, removal from groups, historical tickets retained); verify tests for each scenario in the user-management delta

## 4. Attractions (F2-C05, F2-C06, F2-C07)

- [x] 4.1 Implement `Attraction`, `ATTRACTION CREATE` and `ATTRACTION LIST`; verify tests (invalid capacity, duplicate, empty list)
- [x] 4.2 Implement `ATTRACTION ENTER` through the `Ticket` abstraction (date must equal today, atomic capacity check), `ATTRACTION EXIT`, `ATTRACTION STATUS`; verify tests for individual entry, group entry, insufficient capacity, wrong date, exit of one group member, status fields

## 5. Persistence and --fly (F2-C08)

- [x] 5.1 Implement `GroupRepository`, `AttractionRepository`, `TicketRepository` (with user/group snapshots) and `saveAll` after mutations; verify `ParkPersistenceTest` reload of every element, empty attractions after restart, deleted user's historical ticket and null group reference survive restart
- [x] 5.2 Implement `--fly` argument handling in `App` (timestamped `tmp/` directory, cleanup in `finally`); verify `AppFlyTest` for `--fly <file>` and `--fly` interactive leaving no directory and not touching normal data, and Delivery 1 argument forms unchanged

## 6. UML (F2-C09)

- [x] 6.1 Write `uml/model.puml` covering classes, enum, ticket hierarchy, composition, associations, directions and multiplicities; verify by cross-checking each class/field against the source

## 7. Integration (F2-C10)

- [x] 7.1 Extend `HELP` with the Delivery 2 syntax list; verify test checks all twenty syntaxes
- [x] 7.2 Run `mvn clean package`, then run the JAR with an `input_fase2.txt` exercising every Delivery 2 command in normal and `--fly` mode; verify expected outputs, persisted files in normal mode, and no residue in `--fly` mode
