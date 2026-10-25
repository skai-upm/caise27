# Tasks

## 1. Ticket model and persistence

- [x] 1.1 Create `Ticket` (id, date, holder, BigDecimal price; `isCurrentOrFuture`, `isRefundable`); verify with `TicketTest` including the today boundary
- [x] 1.2 Create `TicketRepository` (`data/tickets.txt` as `<id>;<date>;<dni>;<price>`, load with a DNI lookup that falls back to a detached historical holder, skip malformed lines); verify with `TicketRepositoryTest`

## 2. Ticket operations

- [x] 2.1 Create `TicketManager` (current price 40.00, `setPrice`, `create` with all error responses, `delete` refund rule, `hasActiveTickets`, persistence); verify with `TicketManagerTest` covering every ticketing scenario and restart
- [x] 2.2 Add `TICKET CREATE`, `TICKET DELETE`, `SYSTEM PRICE` to `CommandProcessor` and HELP, add the active-ticket deletion guard after the credential check (expose `UserManager.checkCredentials`), wire `TicketManager` in `App`; verify with `CommandProcessorTest`/`AppTest` including the spec's deletion scenario with SYSTEM DATE moves

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` with `Ticket`, `TicketManager`, `TicketRepository` and their associations; verify with `plantuml -checkonly`
- [x] 3.2 Verify end to end with `mvn package` and a command file reproducing the proposal example (SYSTEM DATE, TICKET CREATE, blocked USER DELETE, SYSTEM DATE forward, USER DELETE succeeds, SYSTEM PRICE) and a restart showing tickets restored
