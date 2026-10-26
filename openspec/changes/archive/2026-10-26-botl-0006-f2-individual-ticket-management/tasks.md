# Tasks

## 1. Ticket model and persistence

- [x] 1.1 Create `Ticket` (id, date, user, price; current/future and strictly-future checks); verify with unit tests at date boundaries
- [x] 1.2 Create `TicketRepository` (`id;date;price;dni;email;name;surname`, resolve by DNI, detached user for deleted holders); verify with `@TempDir` round-trip tests for both cases

## 2. Ticket rules and commands

- [x] 2.1 Create `TicketManager` (create with validations, delete only strictly-future tickets, price setting 40.00 default with validation, hasCurrentOrFutureTicket); verify with unit tests for every spec scenario
- [x] 2.2 Split `UserManager` deletion into `authenticate` + `delete(User)` and block deletion with current/future tickets in `CommandProcessor`; verify with tests for the blocking and past-ticket scenarios
- [x] 2.3 Add `TICKET CREATE`, `TICKET DELETE`, `SYSTEM PRICE` to `CommandProcessor` and HELP, wire `TicketManager` with `data/tickets.txt` in `App`; verify with command tests and a jar session including a restart

## 3. Model and verification

- [x] 3.1 Update `uml/model.puml` with the ticket classes and relationships; verify with `plantuml -checkonly`
- [x] 3.2 Run `mvn test` and verify all tests pass
