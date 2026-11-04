# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Rename the concrete `Ticket` to `IndividualTicket` and introduce abstract `Ticket` (id, date, `getPrice`, `getPriceText`); verify all existing tests pass after updating references
- [x] 1.2 Create `GroupTicket` (group aggregation, constituent composition, unit price, discounted total, `discountPercent`); verify `GroupTicketTest` for brackets at 1, 2, 3, 5, 6, 10, 11 members and the source totals 80/108/192/220

## 2. Persistence and service

- [x] 2.1 Implement `GroupTicketRepository` (`Id;Grupo;fecha;unit;total;ids`) with resolution of groups and constituents; verify `GroupTicketRepositoryTest` round-trip and missing group kept as id
- [x] 2.2 Extend `TicketService` with group ticket create/delete (two-file save with rollback), shared identifier space, `isGroupLocked`, and constituent protection in `delete`; verify `GroupTicketServiceTest` for composition, frozen membership, rejections leaving files unchanged, deletion with constituents and date boundaries

## 3. Commands and locks

- [x] 3.1 Add `GROUPTICKET CREATE` (prescribed response) and `GROUPTICKET DELETE` to `CommandProcessor`, group lock checks for `GROUP ADD/REMOVE/DELETE`, HELP lines, and `grouptickets.txt` wiring in `App`; verify `GroupTicketCommandsTest` (lock, release as date advances, user deletion blocked by constituent, `SYSTEM PRICE` base) and an `AppTest` restart scenario

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` with the inheritance (`Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`), aggregation to `Group`, composition of constituents and multiplicities; verify `UmlModelTest`, `plantuml -checkonly`, `mvn test` and `mvn package`
