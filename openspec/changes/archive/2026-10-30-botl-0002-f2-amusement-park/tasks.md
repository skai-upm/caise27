# Tasks

## 1. Central park state (F2-C01)

- [x] 1.1 Add `repository.LineFile` and refactor `UserFileStore` onto it; verify existing `UserRepositoryTest` still passes
- [x] 1.2 Add `controller.ParkController` singleton (repositories, date, price, `initialize(dataDir)`) with SYSTEM DATE/PRICE parsing; verify `ParkControllerTest` (same instance, defaults, valid/invalid date and price)

## 2. Groups (F2-C02)

- [x] 2.1 Add `model.Group`, `repository.GroupFileStore`/`GroupRepository` and `controller.GroupController` (create, add, remove, delete with lock check); verify `GroupControllerTest` for success, duplicates, unknown ids and lock behaviour

## 3. Tickets (F2-C03, F2-C04)

- [x] 3.1 Add `model.Ticket`, `IndividualTicket`, `GroupTicket` (price reuse, discount brackets, holders, active/valid/deletable) and verify `TicketTest` including bracket limits and 6×32=192
- [x] 3.2 Add `repository.TicketFileStore`/`TicketRepository` (shared id space incl. compositions, traversal queries, ACTIVE/HISTORIC user snapshots) and verify `TicketRepositoryTest` round-trip incl. deleted user and deleted group
- [x] 3.3 Add `controller.TicketController` (TICKET CREATE/DELETE, GROUPTICKET CREATE/DELETE, atomic id checks) and verify `TicketControllerTest` success and rejection cases
- [x] 3.4 Extend `UserController.delete` with active-ticket and locked-group checks and removal from groups; verify updated `UserControllerTest`

## 4. Attractions (F2-C05, F2-C06, F2-C07)

- [x] 4.1 Add `model.Attraction`, `repository.AttractionFileStore`/`AttractionRepository`, `controller.AttractionController` (create, enter, exit, list, status) and verify `AttractionControllerTest` (individual/group entry, capacity atomicity, invalid date, exit, list empty, status content)

## 5. Commands, persistence and --fly (F2-C08, F2-C10)

- [x] 5.1 Refactor `CommandProcessor` to a registered-command table with the 20 commands and generated HELP; verify `CommandProcessorTest` (all HELP syntaxes, malformed input, new command messages)
- [x] 5.2 Update `App` for data directory and `--fly` (timestamped tmp dir, cleanup on EXIT and end of file); verify `AppTest` (restart reconstructs park state with empty attractions; `--fly` leaves no files and does not touch normal data)

## 6. UML (F2-C09)

- [x] 6.1 Write `uml/model.puml` from the final model classes and verify every named class exists in `src/main/java` with matching relationships

## 7. Delivery 2 integration (F2-C10)

- [x] 7.1 Run `mvn clean package` and execute the JAR with a Delivery 2 command file in normal and `--fly` modes; verify expected outputs and that `--fly` leaves no data behind
