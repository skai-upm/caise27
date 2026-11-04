# Tasks

## 1. Ticket model and persistence

- [x] 1.1 Create immutable `Ticket` (id, user reference, holder DNI, date, `BigDecimal` price scale 2); verify `TicketTest` for field values and two-decimal formatting
- [x] 1.2 Implement `TicketRepository` (`Identificador;DNI;yyyy-MM-dd;Precio`, DNI resolution, unknown DNI kept as history, malformed lines skipped, missing file → empty); verify `TicketRepositoryTest` round-trip, history line and missing file

## 2. Ticket rules

- [x] 2.1 Implement `TicketService` create/delete/`hasCurrentOrFutureTicket` with atomic save and date rules (`>=` for blocking, `>` for deletion, not-before-today for creation); verify `TicketServiceTest` for every `individual-tickets` scenario including boundaries on the same day

## 3. Controller integration

- [x] 3.1 Add `ticketPrice` (40.00) and `SYSTEM PRICE` to `CommandProcessor`, plus `TICKET CREATE` (prescribed response) and `TICKET DELETE`, and the three HELP lines; verify `CommandProcessorTest`/`TicketCommandsTest` for price changes, invalid prices, exact response text and HELP
- [x] 3.2 Enforce the ticket restriction in `USER DELETE` (public `UserService.authenticate`) and wire `tickets.txt` in `App`; verify the source example sequence (`SYSTEM DATE;2026-10-01`, `TICKET CREATE`, `USER DELETE` rejected, `SYSTEM DATE;2026-10-16`, `USER DELETE` accepted) and the restart scenarios in `AppTest`

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` with `Ticket`, `TicketService`, `TicketRepository` and their associations/multiplicities; verify `UmlModelTest` (extended to check `Ticket "0..*" --> "0..1" User`) and `plantuml -checkonly`, and that `mvn test` and `mvn package` succeed
