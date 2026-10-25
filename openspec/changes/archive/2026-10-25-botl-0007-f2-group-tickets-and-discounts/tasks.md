# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Turn `Ticket` into an abstract base (id, date, `getPrice`, `getVisitors`, date rules) and rename the concrete class to `IndividualTicket`; update all code and tests; verify `mvn test` passes unchanged
- [x] 1.2 Create `GroupTicket` (aggregated `Group`, composed `IndividualTicket`s with ids `<id>-<n>`, `discountFor`, discounted total); verify with `GroupTicketTest` covering 2/3, 5/6, 10/11 boundaries and visitors

## 2. Group ticket operations

- [x] 2.1 Extend `TicketManager` (constructor with `GroupManager`; `createGroupTicket`, `deleteGroupTicket`, shared id namespace incl. internal ids, `isGroupLocked`, `hasActiveTickets` via visitors) and `TicketRepository` (`grouptickets.txt` load/save with detached group/holders); verify with `TicketManagerTest`/`TicketRepositoryTest` including restart and frozen composition
- [x] 2.2 Add `GROUPTICKET CREATE`/`GROUPTICKET DELETE` and the group lock checks to `CommandProcessor`, update HELP and `App` wiring; verify with `CommandProcessorTest` (lock/unlock with SYSTEM DATE, member deletion blocked, TICKET DELETE on internal id) and `AppTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` with the hierarchy, aggregation and composition; verify with `plantuml -checkonly`
- [x] 3.2 Verify end to end with `mvn package` and a command file covering the four discount brackets, lock/unlock and refund, plus a restart
