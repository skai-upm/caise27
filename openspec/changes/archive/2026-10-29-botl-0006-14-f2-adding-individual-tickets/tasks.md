# Tasks

## 1. Ticket model

- [x] 1.1 Create abstract `Ticket` (identifier, date, price, `isActiveOn`, `isDeletableOn`, `identifiers()`, abstract `involves(User)`) and `IndividualTicket` holding its `User`; verify with unit tests for active/deletable boundaries (today, before, after) and that `User` has no ticket fields

## 2. Ticket operations in central management

- [x] 2.1 Extract a static strict `yyyy-MM-dd` `parseDate` in `ParkController` reused by `setCurrentDate`; verify existing SYSTEM DATE tests still pass
- [x] 2.2 Add the ticket registry, common `isTicketIdentifierUsed` over `identifiers()`, and `createIndividualTicket` (existing user, date ≥ current date, free identifier, current base price); verify with unit tests for default price 40.00, price after `SYSTEM PRICE;45.50`, unchanged price after a later price change, today allowed, past rejected, unknown user and duplicate identifier rejected
- [x] 2.3 Add `deleteTicket` (exists, strictly future) and `ticketsOf(User)` / `hasActiveTicket(User)` by traversal; verify with unit tests for future deletion, today/past kept, unknown identifier, traversal result, and active→historical after advancing the date

## 3. Commands

- [x] 3.1 Dispatch `TICKET CREATE` (3 arguments, prints `TICKET CREATED. PRICE: <amount> EUR`) and `TICKET DELETE` (1 argument) and add both to HELP; verify with processor tests for the source example `TICKET CREATE;T001;ana@correo.com;2027-04-15`, deletion, rejections and HELP output
- [x] 3.2 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR with `SYSTEM DATE;2027-04-01`, `TICKET CREATE;T001;ana@correo.com;2027-04-15`, `TICKET DELETE;T001`, `EXIT` and verify controlled error output
