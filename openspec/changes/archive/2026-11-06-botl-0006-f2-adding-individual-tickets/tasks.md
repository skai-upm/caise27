# Tasks

## 1. Ticket model

- [x] 1.1 Add `abstract Ticket` (id, date, price, active/deletable checks) and `IndividualTicket` (owner email), and verify with `TicketTest` the active check (date >= today) and deletable check (date > today), and that the price is fixed at creation

## 2. Controller operations

- [x] 2.1 Add ticket storage with the shared identifier check, `createIndividualTicket` (user exists, unused id, date not in the past, current base price) and `ticketsOf(email)`, and verify with `ParkControllerTicketTest` that each rejection leaves no ticket and that a price change does not alter existing tickets
- [x] 2.2 Add `deleteTicket` (only strictly future individual tickets) and verify with tests that today, past and unknown tickets are rejected and remain or are reported
- [x] 2.3 Add `deleteUserReferences(email)` (active tickets or locked groups reject, otherwise remove from groups), and verify with tests that a rejected call changes nothing, historical tickets do not block and are kept, and an allowed call removes the user from groups

## 3. Commands

- [x] 3.1 Add the TICKET CREATE/DELETE handlers (displaying the price) and HELP lines, and verify with `TicketCommandsTest` the confirmations, the date parsing errors, the argument errors and the HELP content
- [x] 3.2 Document the TICKET commands in `README.md`, run `mvn clean package`, and verify that all tests pass and that a JAR run of `TICKET CREATE` for a past date and `TICKET DELETE;T999` gives controlled errors
