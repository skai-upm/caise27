# Tasks

## 1. Model

- [x] 1.1 Add `Ticket.discounted(base, percent)`, the `partOfGroupTicket` flag on `IndividualTicket` and `GroupTicket` (composition, nullable group, summed price, discount brackets), and verify with `GroupTicketTest` the brackets for 2/3/5/6/10/11 members and that the total equals the sum of composed prices

## 2. Controller

- [x] 2.1 Replace the existence predicate with `UserDirectory` (existence + DNI) and remove the stub group-ticket-date lookup in favour of deriving the lock from stored group tickets, and verify that the existing group and ticket tests pass after migrating them to real group tickets
- [x] 2.2 Add `createGroupTicket` (group exists and is non-empty, date not past, identifier and derived identifiers unused, atomic), and verify with tests that a conflict on a derived identifier creates nothing and that the composition is unaffected by later group changes
- [x] 2.3 Add `deleteGroupTicket` (strictly future, removes the composition), make `deleteTicket` reject composed tickets, and make `deleteGroup` clear the references of historical group tickets, and verify each with tests, including unlocking a group after deleting its only future group ticket
- [x] 2.4 Verify with tests that `deleteUserReferences` rejects a user holding an active composed ticket and a member of a locked group, and allows deletion once the group ticket is historical

## 3. Commands

- [x] 3.1 Add the GROUPTICKET CREATE/DELETE handlers (displaying the total) and HELP lines, and verify with `GroupTicketCommandsTest` the 192.00 example, the error cases and the HELP content
- [x] 3.2 Document the GROUPTICKET commands and discounts in `README.md`, and verify that `mvn clean package` passes
