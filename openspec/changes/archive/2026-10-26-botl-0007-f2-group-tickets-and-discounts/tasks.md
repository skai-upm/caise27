# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Make `Ticket` abstract and move the individual behaviour to `IndividualTicket`; update existing code/tests; verify `mvn test` still passes
- [x] 1.2 Create `GroupTicket` (group, internal tickets, total) with `discountFor` and `getUsers`; verify unit tests for all brackets and boundaries 2/3, 5/6, 10/11

## 2. Group ticket rules

- [x] 2.1 Add `GroupManager.addModificationCheck` used by add/remove/delete; verify a unit test where a registered check blocks each operation
- [x] 2.2 Extend `TicketManager` with `createGroupTicket` (existing non-empty group, unique ids incl. internal, date rule, one ticket per member, discounted total) and `deleteGroupTicket` (strictly future, cascades internal tickets); register the group lock check; verify unit tests for purchase, fixed composition, cascade deletion, lock and release, and user-deletion blocking
- [x] 2.3 Reject `TICKET DELETE` of internal tickets; verify a unit test

## 3. Persistence, commands and model

- [x] 3.1 Persist group tickets in `data/grouptickets.txt` and restore links to group and internal tickets (detached group if missing); verify a restart test
- [x] 3.2 Add `GROUPTICKET CREATE/DELETE` to `CommandProcessor` and HELP, wiring in `App`; verify command tests including the `OK GROUP TICKET CREATED -> 108.00` response, then run `mvn test`
- [x] 3.3 Update `uml/model.puml` with the abstract `Ticket`, `IndividualTicket`, `GroupTicket`, aggregation to `Group` and composition of `IndividualTicket`; verify `plantuml -checkonly` succeeds
