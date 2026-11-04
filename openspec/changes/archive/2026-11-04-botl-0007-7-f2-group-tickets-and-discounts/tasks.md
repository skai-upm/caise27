# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Introduce abstract `Ticket`, rename the concrete class to `IndividualTicket` across production and test code, keeping `tickets.txt` format; verify `mvn test` passes unchanged in behavior
- [x] 1.2 Implement `GroupTicket` (group reference, base price, constituent tickets, `discountPercent`, total); verify with `GroupTicketTest` covering brackets and boundaries 1, 2, 3, 5, 6, 10, 11 at 40.00

## 2. Group ticket management and persistence

- [x] 2.1 Implement `GroupTicketFileStore` (line format, restore links to group and users, history lines and reserved ids); verify with `GroupTicketFileStoreTest`
- [x] 2.2 Extend `TicketManager` with group ticket create/delete, `isGroupLocked`, `allTickets`, constituent-aware `hasTicketOnOrAfter`, cross-kind id uniqueness and constituent protection in `delete`; verify with `TicketManagerTest` (composition, frozen composition, rejections, deletion rules, locks, restart)

## 3. Commands and wiring

- [x] 3.1 Add `GROUPTICKET CREATE|DELETE` (verbatim output), group-lock checks for `GROUP ADD|REMOVE|DELETE`, HELP lines, and `grouptickets.txt` wiring in `App`; verify with `CommandProcessorTest`/`AppTest` (source example, the six bracket examples, lock and release as the date advances, user deletion blocked by constituent ticket, restart restores lock)

## 4. Model and regression

- [x] 4.1 Update `uml/model.puml` with the ticket hierarchy, aggregation, composition and multiplicities; verify with PlantUML `-checkonly`
- [x] 4.2 Verify `mvn test` passes and run the packaged JAR with the source example (`GROUPTICKET CREATE;TG01;G01;2026-10-15`) and a locked `GROUP ADD`
