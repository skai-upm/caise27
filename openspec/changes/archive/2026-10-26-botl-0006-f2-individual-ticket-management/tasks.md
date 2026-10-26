# Tasks

## 1. Ticket model and persistence

- [x] 1.1 Create `Ticket` (id, date, user, price) with `isCurrentOrFuture`/`isFuture` date checks; verify unit tests at the boundary dates
- [x] 1.2 Create `TicketRepository` storing `Id;date;price;DNI` and relinking holders (detached holder for unknown DNI); verify a round-trip test in a temp directory

## 2. Ticket rules and commands

- [x] 2.1 Create `TicketManager` with create (unique id, existing user, date not before system date, current price), delete (only strictly future), `setPrice` (positive, ≤2 decimals, default 40.00) and `hasCurrentOrFutureTickets`; verify unit tests for each rule and restart restore
- [x] 2.2 Add `UserManager.addDeletionCheck` and register the ticket check from `TicketManager`; verify tests that a current/future ticket blocks deletion and a past ticket does not (ticket kept)
- [x] 2.3 Add `TICKET CREATE`, `TICKET DELETE`, `SYSTEM PRICE` to `CommandProcessor` and HELP, wire `data/tickets.txt`; verify command tests including the `OK TICKET CREATED -> 40.00` / `60.00` responses and parameter errors
- [x] 2.4 Run `mvn test` and verify the full suite passes

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with `Ticket` (→ 1 `User`), `TicketManager`, `TicketRepository`; verify `plantuml -checkonly` succeeds and every main class is named
