# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Make `Ticket` abstract, move the individual behavior to `IndividualTicket`, add `GroupTicket` (aggregation to Group, composition of internal tickets, discount brackets); verify with `GroupTicketTest` covering 2/3, 5/6, 10/11 boundaries and the four brackets, and existing tests updated and passing

## 2. Persistence and rules

- [x] 2.1 Extend `TicketStorage` with `data/group_tickets.txt` (`Id;date;IdGrupo;PrecioBase;Descuento;Id-n:DNI,...`) restoring groups and users; verify with `TicketStorageTest` group round-trip and history with missing group/users
- [x] 2.2 Extend `TicketManager` with group ticket create/delete, unique ids across kinds, `isGroupLocked`, `detachGroup`, internal tickets in `hasTicketOnOrAfter`/`detachOwner`, and TICKET DELETE refusing internal tickets; verify with `TicketManagerTest` cases

## 3. Commands, wiring and model

- [x] 3.1 Add `GROUPTICKET CREATE|DELETE` (exact `OK GROUP TICKET CREATED -> <Precio>`), group lock checks before GROUP ADD/REMOVE/DELETE, group detachment on GROUP DELETE, HELP lines and App wiring; verify with `CommandProcessorTest`/`AppTest` scenarios (lock while current, unlock after date, user deletion blocked by internal ticket, restart) and `mvn test`
- [x] 3.2 Update `docs/uml/model.puml` with the ticket inheritance, GroupTicket–Group aggregation, GroupTicket–IndividualTicket composition and multiplicities; verify every class appears and `plantuml -checkonly` passes
