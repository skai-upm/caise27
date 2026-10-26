# Tasks

## 1. Domain

- [x] 1.1 Add abstract `Ticket.getHolders()` implemented by `IndividualTicket` and `GroupTicket`; verify with unit tests
- [x] 1.2 Create `Attraction` (visitors set, atomic `enter(Ticket, today)`, `exit(Ticket)`, `available()`); verify with unit tests for date, duplicates, capacity, atomic group rejection and exit

## 2. Management, persistence and commands

- [x] 2.1 Create `AttractionRepository` (`id;name;capacity`) and `AttractionManager` (create/delete/enter/exit with validations); verify with unit tests including restart with empty attractions
- [x] 2.2 Add `ATTRACTION CREATE/DELETE/ENTER/EXIT/STATUS` to `CommandProcessor` and HELP, wire in `App`; verify with command tests and a jar session reproducing the proposal's status example

## 3. Model and verification

- [x] 3.1 Update `uml/model.puml` with attraction classes and relationships; verify with `plantuml -checkonly`
- [x] 3.2 Run `mvn test` and verify all tests pass
