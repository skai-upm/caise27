# Tasks

## 1. Central controller, date and price (F2-C01)

- [x] 1.1 Rename `UserOperationException` to `OperationException`, make `UserService.authenticate` public with `remove(User)`, and verify existing tests still pass
- [x] 1.2 Add `Storage` (data directory → four file paths) and singleton `ParkController` with `load`, current date (local date) and price (40.00), and verify `getInstance()` identity and defaults in `ParkControllerTest`
- [x] 1.3 Implement SYSTEM DATE and SYSTEM PRICE (validation, no change on error, price affects only new tickets) and verify in `ParkControllerTest`

## 2. Groups (F2-C02)

- [x] 2.1 Implement `Group` and GROUP CREATE / ADD / REMOVE with uniqueness, duplicate-member and existence checks, and verify in `GroupTest`

## 3. Tickets (F2-C03, F2-C04)

- [x] 3.1 Implement `Ticket`, `IndividualTicket`, `GroupTicket`, `GroupDiscount` with shared price behaviour and verify brackets and 6×32=192 in `TicketTest`
- [x] 3.2 Implement TICKET CREATE / DELETE (shared id space, past dates, strictly-future deletion, composition tickets protected) and verify in `TicketingTest`
- [x] 3.3 Implement GROUPTICKET CREATE / DELETE (non-empty group, derived ids checked before creation, fixed composition, cascade delete) and verify in `TicketingTest`
- [x] 3.4 Implement derived group lock, GROUP DELETE (clears group reference of historical tickets) and lock checks on GROUP ADD/REMOVE, and verify in `GroupTest`
- [x] 3.5 Move USER DELETE into `ParkController` with active-ticket, locked-group and group-removal rules, and verify in `UserDeletionTest`

## 4. Attractions (F2-C05, F2-C06, F2-C07)

- [x] 4.1 Implement `Attraction` with ATTRACTION CREATE and LIST and verify in `AttractionTest`
- [x] 4.2 Implement ATTRACTION ENTER via the common ticket abstraction with date and atomic capacity checks, plus EXIT and STATUS, and verify in `AttractionTest`

## 5. Persistence and --fly (F2-C08)

- [x] 5.1 Implement `GroupRepository`, `TicketRepository`, `AttractionRepository`, wire loading/saving in `ParkController`, and verify full round-trip (including deleted user/group history and empty attractions) in `ParkPersistenceTest`
- [x] 5.2 Implement `--fly` argument handling in `App` with timestamped temp directory and cleanup, and verify interactive and file fly runs leave no files and do not touch normal data in `AppTest`
- [x] 5.3 Add `groups.txt`, `tickets.txt`, `attractions.txt` and `tmp/` to `.gitignore` and verify with `git check-ignore`

## 6. Commands and HELP (F2-C10)

- [x] 6.1 Dispatch all Delivery 2 commands in `CommandProcessor` with argument-count checks and extend HELP to the required syntax, and verify in `CommandProcessorTest`

## 7. UML (F2-C09)

- [x] 7.1 Write `uml/model.puml` covering every domain class and relationship as implemented, and verify each model class name appears in it with a test

## 8. Delivery 2 integration (F2-C10)

- [x] 8.1 Run `mvn clean package` and verify all tests pass
- [x] 8.2 Run an end-to-end `input_fase2.txt` scenario with the JAR in normal and `--fly` modes and verify outputs, persisted files, and that `--fly` leaves no data
