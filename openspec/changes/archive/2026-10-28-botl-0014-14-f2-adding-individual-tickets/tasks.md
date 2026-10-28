# Tasks

## 1. Ticket model

- [x] 1.1 Create abstract `model.Ticket` and `model.IndividualTicket`; verify unit tests for price, active/historical and deletable rules at date boundaries
- [x] 1.2 Create `repository.TicketRepository` with the common identifier check covering composition tickets; verify unit tests

## 2. Ticket operations

- [x] 2.1 Create `service.TicketService` (create with current price and date rules, delete only strictly-future standalone tickets, active-ticket lookup by user); verify unit tests for each rule
- [x] 2.2 Make `UserService.updateUser` modify the existing user object after a successful save; verify existing update tests and a test that a ticket sees the updated email
- [x] 2.3 Extend `ParkController` with tickets and make `deleteUser` reject users with active tickets or in locked groups; verify unit tests including historical-only tickets (the locked-group path is unit-tested in GroupService and end to end in F2-C04, when group tickets exist)
- [x] 2.4 Handle `TICKET CREATE` / `TICKET DELETE` in `CommandProcessor` (price printed) and list them in `HELP`; verify processor tests
- [x] 2.5 Run `mvn package` and a JAR session (SYSTEM DATE, create users, tickets at 40.00 and 45.50, deletions, USER DELETE blocked); verify responses

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F2-C03] Add individual tickets`; verify with `git log -1 --format=%s`
