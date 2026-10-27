# Tasks

## 1. Group ticket model

- [x] 1.1 Add discount rate to `Ticket.getPrice()` and create `model/GroupTicket` (composition, nullable group, `discountFor`, summed price, `involves`, `usesId`); verify with `GroupTicketTest` (brackets 2/3/5/6/10/11 members, 6×32=192, rounding, involves)
- [x] 1.2 Make `TicketRepository.isIdAvailable` cover component identifiers and add `findGroupTicketContaining`; add `ParkController.isGroupLocked`; verify with `TicketRepositoryTest`

## 2. Commands

- [x] 2.1 Implement GROUPTICKET CREATE (atomic, derived ids) and GROUPTICKET DELETE, list them in HELP; verify group-tickets scenarios in `GroupTicketCommandsTest`
- [x] 2.2 Reject TICKET DELETE of component tickets; apply locking to GROUP ADD/REMOVE/DELETE (clearing historical tickets' group on delete) and USER DELETE; verify the modified tickets, user-groups and user-deletion scenarios in `GroupTicketCommandsTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F2-C04] Add group ticket purchase`; verify with `git log -1 --format=%s`
