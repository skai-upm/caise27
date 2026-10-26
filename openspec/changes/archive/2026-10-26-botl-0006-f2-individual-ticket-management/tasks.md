# Tasks

## 1. Price and ticket model

- [x] 1.1 Add `ticketPrice` (40.00 at start), `parsePrice` and `formatPrice` to `MainController`; verify `MainControllerTest` (default 40.00, valid `60.00`/`60`, invalid `abc`/`-5`/`0`/`1.234`)
- [x] 1.2 Create `Ticket` and `TicketRepository` (`id;dni;date;price`, users resolved by DNI); verify `TicketRepositoryTest` round-trip, unknown DNI and malformed line skipped

## 2. Ticket rules

- [x] 2.1 Create `TicketManager` (create with user/id/date checks charging the current price, delete only if date > system date, `hasCurrentOrFutureTicket`, `removeTicketsOf`, persistence with reload on failure); verify `TicketManagerTest` covers every ticket-management scenario including past tickets retained and restart keeping prices

## 3. Commands and user deletion

- [x] 3.1 Add TICKET CREATE, TICKET DELETE and SYSTEM PRICE to `CommandProcessor` (listed by HELP), wire `TicketManager` with `data/tickets.txt` in `App` and `TestPark`; verify `CommandProcessorTest` for the three commands and their output formats
- [x] 3.2 Make `UserManager.authenticate` public and block USER DELETE while the user has a ticket dated on or after the system date, removing past tickets on success; verify `CommandProcessorTest` for future, same-day and past ticket scenarios

## 4. Model and end-to-end

- [x] 4.1 Update `uml/model.puml` with `Ticket`, `TicketManager`, `TicketRepository` and `ticketPrice`; verify PlantUML `-checkonly` succeeds
- [x] 4.2 Run `mvn package` and the proposal's example sequence (SYSTEM DATE, TICKET CREATE, USER DELETE rejected, SYSTEM DATE forward, TICKET DELETE rules, SYSTEM PRICE) through the JAR; verify outputs and restart persistence
