# Tasks

## 1. Ticket model

- [x] 1.1 Create abstract `model.Ticket` and `model.IndividualTicket` (user reference, price fixed at purchase); verify unit tests for active/deletable rules and price
- [x] 1.2 Create `repository.TicketRepository` with shared-identifier check `ensureAvailable` and traversal by user; expose from `Park` (reset in `start`); verify unit tests

## 2. Commands

- [x] 2.1 Create `service.TicketService` with create (user exists, date ≥ today, current price) and delete (date > today); register `TICKET CREATE`/`TICKET DELETE` with HELP lines; verify command tests including price changes and date edge cases
- [x] 2.2 Block `USER DELETE` when the user has active tickets; verify tests for active, today, historical-only and unblocked-after-delete cases

## 3. Integration

- [x] 3.1 Run the JAR with SYSTEM DATE, TICKET CREATE/DELETE and USER DELETE sequences; run `mvn clean package` and verify all tests pass
