# Tasks

## 1. Storage foundation

- [x] 1.1 Add `TextFileStore` (line read + atomic write) and `StoragePaths`, and move `UserRepository` onto `TextFileStore`; verify existing repository tests still pass and a new `TextFileStore` test covers round-trip and missing file

## 2. Main controller, system date and price

- [x] 2.1 Add `ParkController` with `currentDate` (OS date at start), `ticketPrice` (40.00), strict date/price parsing, `SYSTEM DATE` and `SYSTEM PRICE`; move user operations behind it (`UserManager.authenticate`/`remove`) and make `CommandProcessor` depend on it; verify with unit tests for valid/invalid dates and prices and the existing user tests

## 3. Groups

- [x] 3.1 Add `Group`, `GroupRepository` and `GroupManager` with create/add/remove/delete, existing-user and duplicate-member checks, and persistence linked to loaded users; wire `GROUP *` commands; verify with unit tests including restart and shared user references

## 4. Individual tickets

- [x] 4.1 Add abstract `Ticket`, `IndividualTicket`, `TicketRepository` and `TicketManager`; implement `TICKET CREATE` (`OK TICKET CREATED -> <price>`), `TICKET DELETE` (future only), unique ids, no past purchases and persistence; verify with unit tests including price change and restart
- [x] 4.2 Apply the ticket rule to `USER DELETE` (current/future tickets block, past tickets and memberships cascade); verify with unit tests for the 2026-10-01 / 2026-10-16 example

## 5. Group tickets

- [x] 5.1 Add `GroupTicket` (aggregation to group, composition of internal tickets, discount brackets) with `GROUPTICKET CREATE`/`DELETE`, persistence and group locking for add/remove/delete; verify with unit tests for 2/3, 5/6, 10/11 boundaries, locking/unlocking, deletion cascade and restart

## 6. Attractions

- [x] 6.1 Add `Attraction`, `AttractionRepository` and `AttractionManager` with create/delete, polymorphic atomic `enter`/`exit`, date check, capacity and `ATTRACTION STATUS`; verify with unit tests for individual/group entry, full-capacity rejection, exit and restart (empty)

## 7. Fly mode and help

- [x] 7.1 Implement `--fly` in `App` (timestamped `tmp/` directory, same repositories, deletion on EXIT/end/error, works with a command file) and add `tmp/` to `.gitignore`; verify with tests that permanent data is untouched and the directory is removed
- [x] 7.2 Update `HELP` with every command and the README command table; verify with a unit test listing all commands

## 8. UML

- [x] 8.1 Write `uml/model.puml` from the implemented classes following the notation rules; verify it with PlantUML `-checkonly` and by cross-checking every class/attribute/relationship against the code

## 9. Integration check

- [x] 9.1 Run `mvn test` and `mvn package`, then run `java -jar target/parque.jar --fly` with a command file exercising date, groups, tickets, group tickets and attractions; verify outputs and that `data/` is untouched
