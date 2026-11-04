# Tasks

## 1. Ticket entity and persistence

- [x] 1.1 Create `Ticket` (id, date, price, ownerDni, nullable user reference with `detachUser`); verify with `TicketTest`
- [x] 1.2 Create `TicketFileRepository` (`Id;yyyy-MM-dd;DNI;Precio`, DNI lookup on load, unknown DNI kept without user, malformed lines skipped, missing file = none); verify with `TicketFileRepositoryTest` (exact line, round-trip with same `User` instance, orphan ticket kept)

## 2. Ticket rules

- [x] 2.1 Create `TicketManager` (create with existence/uniqueness/date >= today checks, delete only when date > today, `hasTicketOnOrAfter`, `detachUser`, save with rollback); verify with `TicketManagerTest` covering every `individual-tickets` scenario and restart recovery

## 3. Commands and wiring

- [x] 3.1 Add `TICKET CREATE` (exact `OK TICKET CREATED -> <Precio>`), `TICKET DELETE`, `SYSTEM PRICE` (regex-free validation, default 40.00) and shared `parseDate` to `CommandProcessor`; update `HELP_LINES`; verify with `CommandProcessorTest` cases for each scenario, including price reset in a new processor
- [x] 3.2 Change `USER DELETE` to authenticate, reject when a ticket is dated on or after the system date, and after deletion remove from groups and detach tickets (`UserManager.authenticate` public); wire `TicketManager` in `App.run` with sibling `tickets.txt`, ignore `tickets.txt`; verify with the source example sequence (SYSTEM DATE 2026-10-01 → blocked; 2026-10-16 → allowed, ticket kept after restart) in `CommandProcessorTest`/`AppTest`

## 4. Model and integration

- [x] 4.1 Update `docs/uml/model.puml` with the ticket classes, owner association and multiplicities; verify every production class appears and PlantUML `-syntax` accepts the file
- [x] 4.2 Run `mvn clean test` (all tests pass) and run the packaged JAR with the source example commands across two runs
