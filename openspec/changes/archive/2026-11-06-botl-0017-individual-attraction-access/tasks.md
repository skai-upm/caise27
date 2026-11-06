# Tasks

## 1. Common abstraction

- [x] 1.1 Add `Ticket.holders()` and `Ticket.isValidOn(date)` (individual: its user; group: composition users) and verify with unit tests

## 2. Access

- [x] 2.1 Add `AttractionService.enter` (validity, already-inside, capacity, all-or-nothing) and verify with unit tests for valid entry, wrong date (past/future), full attraction, already inside, and presence after a date change
- [x] 2.2 Add `TicketService.findIndividualForAccess`, `ParkController.enterAttraction`, dispatch `ATTRACTION ENTER`, list it in `HELP`, and verify with `CommandProcessor` tests including unknown attraction/ticket

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with create user/attraction/ticket/enter/list sequences and verify outputs
- [x] 3.2 Run `openspec validate botl-0017-individual-attraction-access --strict` and verify the change is valid
