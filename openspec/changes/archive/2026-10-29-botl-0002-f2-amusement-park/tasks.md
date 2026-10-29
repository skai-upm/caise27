# Tasks

## 1. Park controller, date and price (F2-C01)

- [x] 1.1 Rename `UserOperationException` to `OperationException`, make `UserService` in-memory (no repository; public `authenticate`, `remove`, `load`), and verify the adapted `UserServiceTest` passes
- [x] 1.2 Add `validation.ValueParser` (strict `uuuu-MM-dd` date, price with ≤ 2 decimals > 0, positive int; no regex) and verify `ValueParserTest` covers valid and invalid inputs from the specs
- [x] 1.3 Add `service.Park` singleton (private constructor, `getInstance`, `initialize(storage, today)`, date/price getters, `setDate`, `setPrice`) and verify `ParkTest` for same-instance, no public constructor, defaults 40.00/today and invalid values rejected

## 2. Groups (F2-C02)

- [x] 2.1 Add `model.Group` and Park operations `createGroup`, `addToGroup`, `removeFromGroup` with derived lock check; verify `ParkGroupTest` for creation, duplicates, unknown group/user, duplicate member and removal

## 3. Tickets (F2-C03, F2-C04)

- [x] 3.1 Add `model.ticket.Ticket` (abstract), `IndividualTicket`, `GroupTicket` with shared price behaviour and `discountFor`; verify `TicketTest` for active/deletable/valid-on rules and discount brackets 2/3/5/6/10/11 members
- [x] 3.2 Add Park `createTicket` / `deleteTicket` (shared id space, date ≥ today, current price, deletion only if date > today and not in a composition); verify `ParkTicketTest`
- [x] 3.3 Add Park `createGroupTicket` / `deleteGroupTicket` / `deleteGroup` (atomic id checks, empty group rejected, composition `<id>:<dni>`, group detach on deletion, lock/unlock behaviour); verify `ParkTicketTest` and `ParkGroupTest` cover the 192.00 example, atomic conflict, fixed composition and lock scenarios
- [x] 3.4 Implement `Park.deleteUser` (active tickets, locked groups, removal from groups/attractions, historical tickets kept); verify `ParkUserTest` covers every `Delete user` scenario

## 4. Attractions (F2-C05, F2-C06, F2-C07)

- [x] 4.1 Add `model.Attraction` and Park `createAttraction`, `listAttraction`, `attractionStatus`; verify `ParkAttractionTest` for creation, invalid capacity/duplicates, empty list and status
- [x] 4.2 Add Park `enterAttraction` (ticket valid today, all-or-nothing capacity, already inside, inactive user) and `exitAttraction`; verify `ParkAttractionTest` for individual/group entry, insufficient capacity, date-change independence and individual exit after group entry

## 5. Persistence and --fly (F2-C08)

- [x] 5.1 Add `persistence.ParkStorage`, `GroupRepository`, `AttractionRepository`, `TicketRepository` and wire `Park.initialize`/`persist`; verify `ParkPersistenceTest` reconstructs groups, tickets (composition, prices, group), attractions empty, and historical tickets of deleted users/groups after re-initialisation
- [x] 5.2 Extend `App` with `--fly [file]` (timestamped `tmp/` directory, same repositories, cleanup in `finally`); verify `AppTest` that fly mode with a file and interactively leaves no `tmp/<timestamp>` directory and does not touch normal-mode files, and that Delivery 1 modes still pass

## 6. Command interface (F2-C10)

- [x] 6.1 Extend `CommandProcessor` to dispatch all Delivery 2 commands with field-count checks and the new HELP text; verify `CommandProcessorTest` for each new command, wrong field counts, and that HELP contains all twenty syntax entries verbatim

## 7. UML model (F2-C09)

- [x] 7.1 Write `uml/model.puml` reflecting the implemented model; verify `UmlModelTest` checks `@startuml`/`@enduml` and that every class/enum in the `model` packages (and `Park`) is declared in the diagram

## 8. Delivery 2 integration (F2-C10)

- [x] 8.1 Run `mvn clean package` and verify all tests pass and `target/nombre-aplicacion.jar` is produced
- [x] 8.2 Run the JAR with an `input_fase2.txt` exercising every Delivery 2 command (normal mode in a scratch directory, then `--fly`) and verify outputs, persisted files in normal mode, and no leftover `tmp/` data after `--fly`
- [x] 8.3 Verify main sources contain no `java.util.regex`, `matches(`, `replaceAll(`, `replaceFirst(`, `Serializable`/`ObjectOutputStream`, and `pom.xml` has no non-test dependencies
