# Tasks

## 1. Model

- [x] 1.1 Add `model.GroupDiscount` brackets and `model.GroupTicket extends Ticket` (fixed composition, nullable group, total = sum); verify unit tests for brackets 2/3/5/6/10/11 and the 6×32 = 192 example

## 2. Ticket operations

- [x] 2.1 Add repository traversals (group tickets of a group, lock state, containing group ticket) and `TicketService.createGroupTicket` with atomic identifier check and `deleteGroupTicket` cascading to the composition; reject `TICKET DELETE` on composition tickets; verify unit tests including conflicts with no partial creation
- [x] 2.2 Register `GROUPTICKET CREATE`/`GROUPTICKET DELETE` with HELP lines; verify command tests

## 3. Groups and users

- [x] 3.1 Reject `GROUP ADD`/`GROUP REMOVE` on locked groups; add `GROUP DELETE` (detaching historical tickets) with HELP line; verify unit tests
- [x] 3.2 Reject `USER DELETE` for users in locked groups or with active composition tickets; verify unit tests

## 4. Integration

- [x] 4.1 Run the JAR with a group ticket sequence (create, conflicts, deletes, locks) and check outputs; run `mvn clean package` and verify all tests pass
