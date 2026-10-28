# Tasks

## 1. Ticket model

- [x] 1.1 Add abstract `Ticket` (identifier, date, price, active/deletable logic, individual-ticket traversal) and `IndividualTicket` (user reference); verify with unit tests for active/historical/deletable by date

## 2. Controller

- [x] 2.1 Add the shared ticket registry with common identifier check and all-or-nothing registration, `createIndividualTicket`, `deleteTicket` and `findTicketsOfUser`; verify with controller unit tests (duplicate id, unknown user, past date, price fixed at creation, deletion rules)
- [x] 2.2 Add derived `hasActiveTickets` and `deleteUser` (lock check, group modifiability check, removal from groups, historical tickets retained); verify with controller unit tests

## 3. Commands

- [x] 3.1 Implement and register `TICKET CREATE` and `TICKET DELETE`, and add them to HELP; verify with command tests covering success output (price) and every rejection, and the HELP test

## 4. Delivery

- [x] 4.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR with a file using SYSTEM DATE, TICKET CREATE with an unknown user and TICKET DELETE of an unknown ticket; verify messages
- [x] 4.2 Commit with the exact message `[F2-C03] Add individual tickets`; verify with `git log -1 --format=%s`
