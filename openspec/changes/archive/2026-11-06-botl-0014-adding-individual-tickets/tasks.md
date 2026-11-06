# Tasks

## 1. Preparation

- [x] 1.1 Add `User.copy()`, replace `UserRepository.replace` with `update(user, change)` (persist copy, then mutate stored object) and use it in `UserService.update`; verify existing user tests (adapted) pass and the same object is updated in place

## 2. Ticket model

- [x] 2.1 Add abstract `Ticket` and `IndividualTicket`, and `TicketRepository` with the single identifier check, and verify with unit tests for common accessors, active/deletable status by date, and duplicate-id detection

## 3. Ticket service and commands

- [x] 3.1 Add `TicketService` (create with current price and date checks, delete only future tickets, `hasActiveTicket`) and verify with unit tests: creation at current/default price, today allowed, past date/unknown user/duplicate id rejected, price change not retroactive, future deletion, today/past deletion rejected
- [x] 3.2 Wire `TicketService` into `ParkController`, dispatch `TICKET CREATE`/`TICKET DELETE`, list them in `HELP`, and verify with `CommandProcessor` tests including the printed price

## 4. User deletion rules

- [x] 4.1 Move `USER DELETE` orchestration to `ParkController.deleteUser` (active ticket, locked group, removal from groups, historical tickets kept) and verify with unit tests for each scenario of the modified `user-deletion` spec

## 5. Integration

- [x] 5.1 Run `mvn package` and verify all tests pass; run the JAR with a date/price/ticket/user-delete sequence and verify outputs
- [x] 5.2 Run `openspec validate botl-0014-adding-individual-tickets --strict` and verify the change is valid
