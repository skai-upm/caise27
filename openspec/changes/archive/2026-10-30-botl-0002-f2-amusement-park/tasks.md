# Tasks

## 1. Central park controller (F2-C01)

- [x] 1.1 Add `Park` singleton (date, price, repositories, `open(dataDir)`) and move `UserService` onto it; verify Delivery 1 tests (adapted to `Park`) still pass and `ParkTest` checks single instance and 40.00/today defaults
- [x] 1.2 Add `SYSTEM DATE` and `SYSTEM PRICE` (strict date, positive amount ≤2 decimals); verify `SystemServiceTest`/`CommandProcessorTest` cover valid and invalid values

## 2. Groups (F2-C02)

- [x] 2.1 Add `Group`, `GroupRepository` (groups.txt) and `GroupService` create/add/remove with duplicate and lock checks; verify `GroupServiceTest` success, duplicates, unknown group/user and persistence after reload

## 3. Tickets (F2-C03, F2-C04)

- [x] 3.1 Add `Ticket` hierarchy (`IndividualTicket`, `GroupTicket`), `TicketRepository` (tickets.txt, shared id space, traversal helpers) and `TicketService` TICKET CREATE/DELETE; verify `TicketServiceTest` price, date rules, id clash, deletion rules
- [x] 3.2 Add GROUPTICKET CREATE/DELETE with `<id>:<dni>` composition, atomic id checks and discounts; verify `TicketServiceTest` brackets 2/3/5/6/10/11, fixed composition, atomic clash, deletion with composition
- [x] 3.3 Add GROUP DELETE and lock enforcement on GROUP ADD/REMOVE; verify `GroupServiceTest` lock by active ticket, unlock after date change or GROUPTICKET DELETE, historical tickets keep data with null group
- [x] 3.4 Extend USER DELETE (active tickets incl. composed, locked groups, removal from groups, history kept); verify `UserServiceTest` new scenarios

## 4. Attractions (F2-C05 – F2-C07)

- [x] 4.1 Add `Attraction`, `AttractionRepository` (attractions.txt, capacity only) and `AttractionService` CREATE/LIST; verify `AttractionServiceTest` create, invalid capacity, duplicate, empty list
- [x] 4.2 Add ENTER (holders via ticket abstraction, date check, capacity, atomic), EXIT and STATUS; verify `AttractionServiceTest` individual/group entry, insufficient capacity, wrong date, already inside, exit, status fields

## 5. Persistence and --fly (F2-C08)

- [x] 5.1 Verify full reload: `ParkPersistenceTest` restores groups/members, individual and group tickets with prices and group link, attractions empty, historical tickets of deleted user and group
- [x] 5.2 Add `--fly [file]` via a `DataDirectory` helper (timestamped `tmp/` dir, cleanup in `finally`) and ignore `tmp/` and new data files in `.gitignore`; verify `AppTest` that permanent files are untouched and the temp dir is removed in file and interactive mode

## 6. Command interface, UML and integration (F2-C09, F2-C10)

- [x] 6.1 Wire all new commands in `CommandProcessor` with argument checks and the Delivery 2 HELP text; verify `CommandProcessorTest` for each command and HELP content
- [x] 6.2 Write `uml/model.puml` from the implemented model; verify `UmlModelTest` that each class/enum in the diagram exists in the code
- [x] 6.3 Run `mvn clean package` and the JAR end-to-end with a Delivery 2 command file in normal and `--fly` mode (incl. restart persistence); verify outputs and that `--fly` leaves no data; update `README.md` with new commands and modes
