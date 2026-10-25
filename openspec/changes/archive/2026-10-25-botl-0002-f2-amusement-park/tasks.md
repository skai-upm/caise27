# Tasks

## 1. Main controller and system settings

- [x] 1.1 Add `ParkException` (make `UserException` extend it), and `ParkController` holding current date, ticket price and `UserManager`, with `load(Path dataDir)`; route existing USER commands through it; verify all F1 tests still pass
- [x] 1.2 Implement `SYSTEM DATE` (strict `yyyy-MM-dd`) and `SYSTEM PRICE` (positive, ≤2 decimals) in controller and processor; verify with unit tests for valid/invalid values and initial values

## 2. Groups

- [x] 2.1 Implement `Group`, `GroupRepository` (`groups.txt`) and `GroupManager` with create/add/remove/delete rules; verify with unit tests (duplicate id, unknown/duplicate member, same User instance, persistence round trip)
- [x] 2.2 Wire `GROUP CREATE/ADD/REMOVE/DELETE` into controller and processor and remove deleted users from their groups; verify with command-level tests

## 3. Tickets

- [x] 3.1 Implement abstract `Ticket`, `IndividualTicket`, `GroupTicket`, `TicketRepository` (`tickets.txt`, `group_tickets.txt`, placeholders for unresolved references) and `TicketManager`; verify with unit tests for persistence round trip including generated tickets
- [x] 3.2 Implement `TICKET CREATE/DELETE` with price, date rules and unique ids, and block `USER DELETE` while the user has a ticket dated ≥ system date; verify with tests for every ticketing scenario
- [x] 3.3 Implement `GROUPTICKET CREATE/DELETE` with per-member ticket generation, discount brackets (2/3, 5/6, 10/11 boundaries), group locking and cascading deletion; verify with tests for every group-ticketing and locked-group scenario

## 4. Attractions

- [x] 4.1 Implement `Attraction` (atomic polymorphic enter, exit), `AttractionRepository` (`attractions.txt`) and `AttractionManager`; verify with unit tests for capacity, atomic group entry, duplicates and exit
- [x] 4.2 Wire `ATTRACTION CREATE/DELETE/ENTER/EXIT/STATUS` with the date check into controller and processor; verify with tests covering the status output after individual, group entries and exit, and restart emptiness

## 5. Fly mode and help

- [x] 5.1 Implement `TemporaryStorage` and `--fly` argument handling in `App` (interactive and file), deleting the directory on EXIT/end/error/shutdown, and add `tmp/` to `.gitignore`; verify with a test running `App.main` with `--fly` and a command file that the temp dir is removed and `data/` is untouched
- [x] 5.2 Extend `HELP` with all Delivery 2 commands and update `README.md`; verify with a test asserting each command syntax appears in the help text

## 6. UML documentation

- [x] 6.1 Write `uml/model.puml` reflecting the implemented classes, hierarchy, associations, aggregation/composition, navigability and multiplicities under the stated conventions; verify it parses with PlantUML (`-checkonly`) and review it against the code

## 7. Integration

- [x] 7.1 Add `input_fase2.txt` exercising all F2 commands, run it with `java -jar target/parque.jar --fly input_fase2.txt`, and confirm `mvn package` passes with all tests
