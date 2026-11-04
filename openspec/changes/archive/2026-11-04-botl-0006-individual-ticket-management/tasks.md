# Tasks

## 1. Ticket model and storage

- [x] 1.1 Create `Ticket` (id, date, BigDecimal price, owner reference, ownerDni) and `TicketFileStorage` (`id;dni;date;price`, resolver on load, owner-less history kept, malformed lines skipped); verify with `TicketFileStorageTest`
- [x] 1.2 Create `TicketManager` (create with unique id/date ≥ today, delete only if date > today, `hasTicketOnOrAfter`, atomic save) loaded against `UserManager`; verify with `TicketManagerTest` including restart identity and history of a deleted owner

## 2. Commands

- [x] 2.1 Add the current price (40.00) and `SYSTEM PRICE` validation to `CommandProcessor`; verify with `CommandProcessorTest` (valid change, invalid values unchanged, new execution resets)
- [x] 2.2 Add `TICKET CREATE` (exact `OK TICKET CREATED -> <price>` reply) and `TICKET DELETE` to `CommandProcessor`, and wire `tickets.txt` in `App`; verify with `CommandProcessorTest` scenarios (nonexistent user, past date, today, duplicate, malformed, future/today/past deletion)
- [x] 2.3 Block `USER DELETE` while a ticket is dated on or after the system date (public `UserManager.authenticate`); verify with `CommandProcessorTest` using the source example (2026-10-01 blocked, 2026-10-16 allowed, ticket kept)
- [x] 2.4 Add the ticket and price commands to HELP; verify with the HELP content test

## 3. Model and regression

- [x] 3.1 Update `docs/uml/model.puml` with `Ticket`, `TicketManager`, `TicketFileStorage` and their associations; verify with the PlantUML `-syntax` check and that every declared class exists
- [x] 3.2 Run `mvn clean package` with all tests passing and a two-execution manual JAR run (restored ticket blocks deletion, price reset)
