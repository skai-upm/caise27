# Tasks

## 1. Abstraction and entry

- [x] 1.1 Add abstract `Ticket.admittedUsers()` implemented by `IndividualTicket` (its user) and `GroupTicket` (composition users); verify unit tests
- [x] 1.2 Add `AttractionService.enter` using the ticket repository and the common abstraction, and register `ATTRACTION ENTER` with a HELP line; verify command tests for valid entry, date before/after, no capacity, unknown ids, already inside and presence after date change

## 2. Integration

- [x] 2.1 Run the JAR with a ticket entry sequence and `ATTRACTION LIST`; run `mvn clean package` and verify all tests pass
