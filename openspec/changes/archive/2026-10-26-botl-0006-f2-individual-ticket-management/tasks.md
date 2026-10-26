# Tasks

## 1. Ticket model and persistence

- [x] 1.1 Create `Ticket` (id, date, price, owner reference, ownerDni, detach) and `TicketStorage` (`Id;yyyy-MM-dd;Precio;DNI`); verify with `TicketStorageTest` round-trip, unresolved DNI kept as history and malformed lines skipped

## 2. Ticket rules

- [x] 2.1 Implement `TicketManager` (create with user/id/date checks, delete only when date > system date, `hasTicketOnOrAfter`, `detachOwner`, persistence after each change); verify with `TicketManagerTest` covering every ticket scenario and restart

## 3. Commands

- [x] 3.1 Add `TICKET CREATE` (exact `OK TICKET CREATED -> <Precio>`), `TICKET DELETE`, `SYSTEM PRICE` with current price 40.00, ticket check before `USER DELETE` plus owner detachment, HELP lines, and `data/tickets.txt` wiring in `App`; verify with `CommandProcessorTest` (example sequence from the proposal, price change, invalid prices, today/past/future deletion rules) and `mvn test`

## 4. Domain model

- [x] 4.1 Update `docs/uml/model.puml` with `Ticket`, `TicketManager`, `TicketStorage` and their associations (`Ticket "0..*" --> "0..1" User`); verify every class in `src/main/java` appears and `plantuml -checkonly` passes
