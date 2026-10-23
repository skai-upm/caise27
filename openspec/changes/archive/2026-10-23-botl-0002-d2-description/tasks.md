# Tasks

## 1. Central park manager and settings

- [x] 1.1 Add `ParkException` (make `UserException` extend it), make `UserService.authenticate` public, and add the `Park` singleton holding `UserService`, system date (today) and base price (40.00) with `SYSTEM DATE`/`SYSTEM PRICE` validation; verify with unit tests for singleton identity, date/price defaults, valid and invalid values
- [x] 1.2 Add `ParkStorage` (data directory from `park.data.dir`, default working directory) building all repositories, extract the shared temp-file write into `TextFiles`, replace `park.users.file`, and wire `Park.open(ParkStorage)`; verify existing Delivery 1 tests pass after adapting them to the new wiring

## 2. Groups

- [x] 2.1 Add `Group` and `Park` group operations (create, add, remove, delete) with derived lock checks; verify with unit tests for each user-groups scenario including duplicate members and locked groups

## 3. Tickets

- [x] 3.1 Add the ticket hierarchy (`Ticket`, `IndividualTicket`, `GroupTicket`, `GroupDiscount`) with shared price/holder behaviour; verify with unit tests for prices, discount brackets (2, 3, 6, 11 members) and the 6-member 192.00 example
- [x] 3.2 Add `Park` ticket operations (TICKET CREATE/DELETE, GROUPTICKET CREATE/DELETE) with the shared identifier index, atomic group creation and date rules; verify with unit tests for each tickets scenario including derived-id conflicts and member-ticket deletion rejection
- [x] 3.3 Extend `USER DELETE` with active-ticket and locked-group checks and removal from groups, keeping historical tickets; verify with unit tests for each modified user-deletion scenario

## 4. Attractions

- [x] 4.1 Add `Attraction` and `Park` attraction operations (create, enter with any ticket type all-or-nothing, exit, list, status); verify with unit tests for each attractions scenario

## 5. Persistence and execution modes

- [x] 5.1 Add `GroupRepository`, `TicketRepository` (with active/former user snapshots and nullable group) and `AttractionRepository`, saving after every park mutation; verify with unit tests for round-trip restart, empty attractions after restart, deleted user/group historical records, and malformed lines
- [x] 5.2 Add `--fly` handling in `App` (timestamped `tmp/` directory, same repositories, cleanup on EXIT/end of input/end of file); verify with unit tests that the normal data files are untouched and no temporary files remain, interactively and with a file

## 6. Command interface and documentation

- [x] 6.1 Extend `CommandProcessor` dispatch and `HELP` with all Delivery 2 commands; verify with unit tests for dispatch, argument-count errors and HELP content
- [x] 6.2 Add `uml/model.puml` covering classes, interfaces/abstract class, enums, inheritance, composition, directed associations and multiplicities matching the code; verify every model class and relationship in the code appears in the diagram
- [x] 6.3 Update `README.md` with the new commands, data files and `--fly` mode; verify the documented commands run as written

## 7. Integration

- [x] 7.1 Run `mvn clean package` and execute the assignment's example sequence with the JAR in normal and `--fly` modes, checking the expected outputs (group discount totals, entry/exit, deletions), data files after a normal run, and absence of temporary files after the `--fly` run
