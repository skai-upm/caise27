# Tasks

## 1. Model

- [x] 1.1 Move price behavior into `Ticket` (base price + discount, rounded) keeping individual tickets unchanged, and verify with unit tests
- [x] 1.2 Add `GroupDiscount` brackets and `GroupTicket` (composition, ids, involves, total = sum, detachable group), and verify with unit tests for brackets 2/3/5/10/11 members and the 6-member 192.00 example

## 2. Service

- [x] 2.1 Add `TicketService.createGroupTicket` (all-or-nothing id checks, non-empty group, date), `deleteGroupTicket` (future only), `hasActiveGroupTicket`, `detachGroup`, and constituent protection in `deleteIndividual`; verify with unit tests for each `group-tickets` scenario
- [x] 2.2 Wire the real active-ticket predicate into `GroupService` and add `ParkController.deleteGroup`; verify with unit tests that groups lock/unlock with group tickets, users with constituent tickets cannot be deleted, and historical group tickets survive group deletion with a cleared reference

## 3. Commands

- [x] 3.1 Dispatch `GROUPTICKET CREATE`/`DELETE`, route `GROUP DELETE` through `ParkController`, list them in `HELP`, and verify with `CommandProcessor` tests including the printed total

## 4. Integration

- [x] 4.1 Run `mvn package` and verify all tests pass; run the JAR with a 6-member group ticket sequence and verify the 192.00 total and locking messages
- [x] 4.2 Run `openspec validate botl-0015-group-tickets --strict` and verify the change is valid
