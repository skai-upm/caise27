# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Turn `Ticket` into an abstract base (`id`, `date`, `price`, `getUsers()`), add `IndividualTicket` (user) and `GroupTicket` (group, internal tickets, `discountFor`, `totalFor`); update existing code/tests to `IndividualTicket`; verify `GroupTicketTest` checks all bracket boundaries (2/3, 5/6, 10/11) and `getUsers` for both kinds
- [x] 1.2 Extend `TicketRepository` to two files (individual lines with optional group-ticket id, `data/grouptickets.txt`) returning `TicketData`; verify `TicketRepositoryTest` round-trips a group ticket with its composition, still loads 4-field lines, and skips unresolvable lines

## 2. Group ticket rules

- [x] 2.1 Add `createGroupTicket`, `deleteGroupTicket`, `isGroupLocked`, `removeGroupTicketsOf` to `TicketManager`, shared id space, internal-ticket rejection in `delete`, and composition clean-up in `removeTicketsOf`; verify `TicketManagerTest` for creation, snapshot, empty/unknown group, duplicate ids, deletion rules, lock dates and restart

## 3. Commands

- [x] 3.1 Add GROUPTICKET CREATE/DELETE to `CommandProcessor` (listed by HELP), lock checks before GROUP ADD/REMOVE/DELETE, group-ticket clean-up after GROUP DELETE; wire `App`/`TestPark` with `data/grouptickets.txt`; verify `CommandProcessorTest` for the proposal's examples, lock lifting as the date advances, and user deletion blocked by internal tickets

## 4. Model and end-to-end

- [x] 4.1 Update `uml/model.puml` with the ticket hierarchy, aggregation, composition and multiplicities; verify PlantUML `-checkonly`
- [x] 4.2 Run `mvn package` and an end-to-end JAR scenario (group of 3 with discount, locks, GROUPTICKET DELETE, restart); verify outputs and data files
