# Tasks

## 1. Model

- [x] 1.1 Add DNI to `User` and update existing tests; verify the test suite still passes
- [x] 1.2 Move price summation into `Ticket.getPrice()` with `IndividualTicket` unit price, and add `GroupTicket` (group reference, fixed composition, discount brackets, derived identifiers); verify with unit tests for 2/3/5/6/10/11 members and 6×32=192

## 2. Controller

- [x] 2.1 Add `createGroupTicket`, `deleteGroupTicket`, `deleteGroup` and ticket-derived `isGroupModifiable`; verify with controller tests (empty group, past date, derived id conflict without partial creation, deletion date rules, group lock/unlock, group deletion with historical tickets, member deletion blocked)

## 3. Commands

- [x] 3.1 Implement and register `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `GROUP DELETE`, and add them to HELP; verify with command tests and the HELP test

## 4. Delivery

- [x] 4.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR with a file using `GROUP CREATE`, `GROUPTICKET CREATE` on the empty group and `GROUP DELETE`; verify messages
- [x] 4.2 Commit with the exact message `[F2-C04] Add group ticket purchase`; verify with `git log -1 --format=%s`
