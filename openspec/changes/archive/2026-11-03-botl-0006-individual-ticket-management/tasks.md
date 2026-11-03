# Tasks

## 1. Ticket model, price and service

- [x] 1.1 Create `Ticket` (id, date, BigDecimal price scale 2, optional user reference, owner DNI) and verify `TicketTest`
- [x] 1.2 Add `currentPrice` (40.00) and `SYSTEM PRICE;Precio` to `CommandProcessor` (non-negative, ≤ 2 decimals); verify tests for 60.00, abc, -5, 10.999 and missing parameter
- [x] 1.3 Create `TicketService` (create with id/user/date checks and captured price, delete only if date > today, hasActiveTickets with date >= today) saving after each change with revert; verify `TicketServiceTest` covers every rule including today boundaries

## 2. Persistence

- [x] 2.1 Create `TicketFileStorage` (`Id;DNI;yyyy-MM-dd;Precio`, link by DNI, keep unresolved DNIs as history, skip malformed) and verify `TicketFileStorageTest` round trip and history line preserved

## 3. Commands and user deletion rule

- [x] 3.1 Add `TICKET CREATE` (response `OK TICKET CREATED -> <Precio>`) and `TICKET DELETE` to `CommandProcessor` and HELP; wire `TicketService` in `App`; add `tickets.txt` to `.gitignore`; verify command tests and the 13 HELP entries
- [x] 3.2 Make `UserService.authenticate` public and reject USER DELETE when the user has tickets dated on or after the system date; verify the source example (2026-10-01 / T001 2026-10-15 rejected, after `SYSTEM DATE;2026-10-16` allowed), the same-day boundary and that past tickets remain after deletion

## 4. UML

- [x] 4.1 Update `uml/model.puml` with `Ticket`, `TicketService`, `TicketFileStorage`, the price attribute and relationships; verify `plantuml -checkonly`

## 5. Integration verification

- [x] 5.1 Verify restart: an `AppTest` buys tickets, changes the price, restarts over the same data directory and checks restored tickets, prices, the deletion block and a historical ticket of a deleted user; then `mvn clean test` passes
