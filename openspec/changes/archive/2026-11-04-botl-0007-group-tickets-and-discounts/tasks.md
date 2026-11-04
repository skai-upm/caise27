# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Introduce abstract `Ticket` (id, date, `getPrice()`, `isOnOrAfter`) and rename the existing class to `IndividualTicket extends Ticket` across sources and tests; verify `mvn test` passes unchanged in behaviour
- [x] 1.2 Create `GroupTicket extends Ticket` (group aggregation with kept `groupId`, constituent `IndividualTicket`s, captured base/total price, static `discountPercent`, `detachGroup`); verify with `GroupTicketTest` (brackets at 1, 2, 3, 5, 6, 10, 11 members → 40, 80, 108, 180, 192, 320, 220 at 40.00)

## 2. Persistence

- [x] 2.1 Create `GroupTicketFileRepository` (`Id;date;GroupId;base;total;DNI...`, lookups for group and users, missing links kept as ids, malformed lines skipped); verify with `GroupTicketFileRepositoryTest` (exact line, round-trip with same objects, orphans)

## 3. Ticket rules

- [x] 3.1 Extend `TicketManager` with group tickets (create with snapshot of current members, cross-kind id uniqueness, delete only future with parts, `groupHasTicketOnOrAfter`, constituent-aware `hasTicketOnOrAfter`/`detachUser`, `detachGroup`, TICKET DELETE rejecting group/constituent ids, rollback); verify with `TicketManagerTest` cases

## 4. Commands and wiring

- [x] 4.1 Add `GROUPTICKET CREATE` (exact `OK GROUP TICKET CREATED -> <Precio>`) and `GROUPTICKET DELETE` to `CommandProcessor`, the group lock before `GROUP ADD/REMOVE/DELETE`, `detachGroup` after group deletion, `HELP_LINES`; wire `grouptickets.txt` in `App.run` and `.gitignore`; verify with `CommandProcessorTest`/`AppTest` (every `group-tickets` scenario, user deletion with group tickets, lock released after the date, restart recovery)

## 5. Model and integration

- [x] 5.1 Update `docs/uml/model.puml` (abstract `Ticket`, inheritance, `GroupTicket *-- "1..*" IndividualTicket`, `GroupTicket o-- Group`, multiplicities); verify all production classes appear and PlantUML `-syntax` accepts it
- [x] 5.2 Run `mvn clean test` and run the packaged JAR with a group-ticket scenario across two runs
