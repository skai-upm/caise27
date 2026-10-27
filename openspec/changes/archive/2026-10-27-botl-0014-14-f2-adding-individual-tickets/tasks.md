# Tasks

## 1. Ticket model

- [x] 1.1 Create abstract `Ticket`, `IndividualTicket` and `TicketRepository` (shared identifier space, traversal by user) and register tickets in `ParkController`; verify with `TicketTest` active/deletable date logic, price, and repository identifier uniqueness

## 2. Ticket operations

- [x] 2.1 Create `TicketService` (create, delete) with `TicketOperationException` and verify with `TicketServiceTest`: default/configured price, price fixed after SYSTEM PRICE, today allowed, past rejected, unknown user, duplicate id, future deletion, today/past deletion rejected, unknown ticket
- [x] 2.2 Make `USER DELETE` reject users with an active ticket and verify with `UserServiceTest`/`TicketServiceTest`: active blocks, deleting the future ticket first allows deletion, historical ticket does not block and keeps the user data

## 3. TICKET commands

- [x] 3.1 Dispatch `TICKET CREATE` / `TICKET DELETE` in `CommandProcessor` (exact element counts, strict date parsing, price output) and add them to HELP; verify with `CommandProcessorTest` and run `mvn package` successfully
