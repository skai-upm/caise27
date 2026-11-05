# Tasks

## 1. Model

- [x] 1.1 Create `model.GroupTicket extends Ticket` (unmodifiable composition, nullable group with `detachGroup`, total = sum of composition, static discount bracket helper); verify `GroupTicketTest` for bracket boundaries (2,3,5,6,10,11 members), 6×40 → 32.00 each / 192.00 total, composition immutability

## 2. Park queries

- [x] 2.1 Implement `hasActiveGroupTicket(Group)`, `isPartOfGroupTicket(Ticket)` and a group-ticket lookup by traversal in `ParkController`; verify tests that a future/today group ticket locks and a historical one does not

## 3. Commands

- [x] 3.1 Implement `GROUPTICKET CREATE` (validation, atomic id check, component creation, response with total) and `GROUPTICKET DELETE` (strictly future, removes composition) in `TicketCommands`, dispatched from `CommandProcessor`, and list both in `HELP`; verify `GroupTicketCommandsTest` for every group-tickets scenario including derived-id conflict leaving nothing created
- [x] 3.2 Make `GROUP DELETE` detach historical group tickets; verify lock tests in the user-groups delta (ADD/REMOVE/DELETE/USER DELETE rejected while locked, unlocked after GROUPTICKET DELETE, today keeps lock, history survives group deletion with null group)

## 4. Integration

- [x] 4.1 End-to-end with the JAR: six users, group, group ticket at 40.00 (total 192.00), conflict case, lock checks, deletion; verify responses and exit status 0; full `mvn package` regression
