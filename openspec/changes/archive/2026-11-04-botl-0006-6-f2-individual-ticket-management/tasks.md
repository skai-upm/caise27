# Tasks

## 1. Building blocks

- [x] 1.1 Add the `verbatim` component and `Result.verbatim` factory, and `UserManager.authenticate` returning the matching user; verify with existing tests plus new `ResultTest`/`UserManagerTest` cases (verbatim `toString`, authenticate success/failure)

## 2. Tickets

- [x] 2.1 Implement `Ticket` and `TicketFileStore` (`Id;yyyy-MM-dd;Precio;DNI`, link by DNI, unresolved lines returned separately as history); verify with `TicketFileStoreTest` (exact line, restore links same `User`, history lines preserved, missing file)
- [x] 2.2 Implement `TicketManager` create/delete/hasTicketOnOrAfter with id uniqueness (including history), date rules and save-then-swap; verify with `TicketManagerTest` (all acceptance scenarios, boundaries today/yesterday/tomorrow, file unchanged on rejection, restart)

## 3. Commands and wiring

- [x] 3.1 Add current price, `SYSTEM PRICE`, `TICKET CREATE` (verbatim output), `TICKET DELETE`, the ticket check in `USER DELETE`, and HELP lines to `CommandProcessor`; refactor `App.run` to a data directory with `tickets.txt`; verify with `CommandProcessorTest`/`AppTest` (source example sequence, price change, invalid prices, deletion blocked/allowed, history after user deletion and restart, price reset on restart)

## 4. Model and regression

- [x] 4.1 Update `uml/model.puml` with `Ticket`, `TicketManager`, `TicketFileStore` and relationships; verify with PlantUML `-checkonly`
- [x] 4.2 Verify `mvn test` passes and a packaged-JAR run reproduces the source example (`SYSTEM DATE;2026-10-01`, `TICKET CREATE;T001;ana@correo.com;2026-10-15`, rejected `USER DELETE`, `SYSTEM DATE;2026-10-16`, accepted `USER DELETE`)
