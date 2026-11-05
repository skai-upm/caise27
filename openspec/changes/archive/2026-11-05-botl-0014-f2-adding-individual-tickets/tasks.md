# Tasks

## 1. Model and store

- [x] 1.1 Create abstract `model.Ticket` (id, date, price, `isActiveOn`, `isDeletableOn`) and `model.IndividualTicket` (user reference); verify `TicketTest` for active/historical/deletable boundaries
- [x] 1.2 Create `repository.TicketRepository` (shared id space, `individualTicketsOf(User)` by traversal) and add tickets plus `hasActiveTicket(User)` / `isPartOfGroupTicket(Ticket)` to `ParkController` (reset per execution); verify repository and controller tests

## 2. Commands

- [x] 2.1 Implement `TICKET CREATE` and `TICKET DELETE` in `command.TicketCommands`, dispatched from `CommandProcessor`; verify `TicketCommandsTest` for every scenario in the tickets spec (initial and configured price, price change not retroactive, today allowed, past/unknown user/invalid date/empty id/duplicate id rejected, future delete, today/past delete rejected, unknown delete)
- [x] 2.2 Extend `USER DELETE` with the active-ticket and locked-group checks; verify tests: future ticket blocks, deletion after deleting the ticket, historical ticket does not block and keeps its user
- [x] 2.3 List ticket commands in `HELP`; verify the help test

## 3. Integration

- [x] 3.1 End-to-end with the JAR: set system date/price, create tickets (price shown), duplicate id, past date, user deletion blocked then allowed after `TICKET DELETE`; verify responses and exit status 0; full `mvn package` regression
