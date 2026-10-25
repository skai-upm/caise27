# Tasks

## 1. Main controller and system configuration

- [x] 1.1 Create `ParkController` holding the current date (OS date at startup) and ticket price (40.00) and route existing USER commands through it from `CommandProcessor`; verify existing tests still pass
- [x] 1.2 Implement `SYSTEM DATE` (strict `yyyy-MM-dd`) and `SYSTEM PRICE` (positive decimal); verify with unit tests for valid, backwards, invalid-format, impossible-date and invalid-price cases

## 2. Groups

- [x] 2.1 Implement `Group`, `GroupManager` and `GroupRepository` (`id;name;dni,...`) with GROUP CREATE/ADD/REMOVE/DELETE rules (unique id, existing users, no duplicates, shared user objects); verify with unit tests including persistence round trip

## 3. Tickets

- [x] 3.1 Implement `Ticket`, `IndividualTicket`, `GroupTicket` (with discount brackets) and `TicketManager`; verify discount unit tests at 2/3, 5/6 and 10/11 members
- [x] 3.2 Implement TICKET CREATE/DELETE and GROUPTICKET CREATE/DELETE in the controller (unique ids, future-only deletion, internal tickets, frozen composition, `-> <Precio>` replies); verify with unit tests
- [x] 3.3 Implement group locking and the new USER DELETE rule (active tickets block, removal from groups); verify with unit tests following the SYSTEM DATE examples in the spec
- [x] 3.4 Implement `TicketRepository` (tickets and group tickets files with user/group snapshots) and startup loading order; verify restart tests keep tickets, prices, compositions and history of deleted users

## 4. Attractions

- [x] 4.1 Implement `Attraction`, `AttractionManager` and `AttractionRepository` with CREATE/DELETE/ENTER/EXIT/STATUS (date check, capacity, atomic group entry, empty at startup); verify with unit tests including the 3-places/4-users case and status after exit

## 5. Execution modes, help and --fly

- [x] 5.1 Update HELP with all commands and wire all repositories from a data directory in `App`; verify HELP test lists every command
- [x] 5.2 Implement `--fly` (timestamped `tmp/` directory, same repositories, deletion on EXIT/end of input, shutdown hook) for interactive and file modes, add `tmp/` to `.gitignore`; verify tests that `data/` is untouched and the temp directory is removed
- [x] 5.3 Update README with the new commands and `--fly`; verify the documented examples run

## 6. Design model

- [x] 6.1 Write `uml/model.puml` from the final code following the diagram conventions; verify it with the PlantUML `-syntax` check and by comparing it with the classes

## 7. Integration

- [x] 7.1 Run `mvn test` and `mvn package`, then run the JAR with a command file covering Delivery 1 and 2 commands, normal and `--fly`, and a restart; verify outputs match the specs
