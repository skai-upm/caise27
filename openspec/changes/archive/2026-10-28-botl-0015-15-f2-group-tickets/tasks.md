# Tasks

## 1. Group ticket model

- [x] 1.1 Add base price + discount to `IndividualTicket` (price rounded to cents) and create `model.GroupTicket` with derived composition identifiers, discount brackets, total price and detachable group reference; verify unit tests for brackets 2/3/5/6/10/11, the 6×40 → 192.00 example and fixed composition

## 2. Operations

- [x] 2.1 Add `TicketService.createGroupTicket` (group exists and non-empty, date not past, atomic check of all identifiers) and `deleteGroupTicket` (strictly future, removes composition); reject group-ticket ids and composition tickets in `TICKET DELETE`; verify unit tests including a derived-id conflict leaving nothing created
- [x] 2.2 Add `TicketService.hasActiveGroupTicket` and wire it as the group lock in `ParkController`; make `ParkController.deleteGroup` detach historical group tickets; verify tests: lock while active, unlock after deletion or when historical, USER DELETE of a member of a locked group rejected
- [x] 2.3 Handle `GROUPTICKET CREATE` / `GROUPTICKET DELETE` in `CommandProcessor`, route `GROUP DELETE` through the controller and list the commands in `HELP`; verify processor tests
- [x] 2.4 Run `mvn package` and a JAR session with a 6-member group at 40.00 and lock/unlock steps; verify `BUILD SUCCESS` and the 192.00 total

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F2-C04] Add group ticket purchase`; verify with `git log -1 --format=%s`
