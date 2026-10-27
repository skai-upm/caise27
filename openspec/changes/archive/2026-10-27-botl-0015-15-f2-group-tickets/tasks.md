# Tasks

## 1. Group ticket model

- [x] 1.1 Create `GroupTicket` (group reference, unmodifiable composition, summed price, `discountFor`) and composition ownership in `IndividualTicket`; verify with `GroupTicketTest` the discount brackets (2, 3, 5, 6, 10, 11 members), 6 × 32.00 = 192.00 and composition immutability

## 2. Group ticket operations and locks

- [x] 2.1 Implement `TicketService.createGroupTicket` / `deleteGroupTicket` (atomic identifier checks, composed tickets saved, cascade deletion) and verify with `TicketServiceTest`: creation, empty/unknown group, past date, derived id conflict without partial creation, future deletion with composition, today kept, composed ticket not deletable via TICKET DELETE
- [x] 2.2 Implement `ParkController.hasActiveGroupTicket`, clear group references on GROUP DELETE, and block USER DELETE for members of locked groups; verify with `GroupServiceTest`/`TicketServiceTest`: locked add/remove/delete, unlock after GROUPTICKET DELETE, historical tickets do not lock, group deletion keeps historical ticket with null group, user deletion blocked/allowed as specified

## 3. GROUPTICKET commands

- [x] 3.1 Dispatch `GROUPTICKET CREATE` / `GROUPTICKET DELETE` in `CommandProcessor` and add them to HELP; verify with `CommandProcessorTest` total price output and errors, then run `mvn package` successfully
