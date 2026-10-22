# Tasks

## 1. Central park state

- [x] 1.1 Add `SystemState` (date = today, price = 40.00, positive ≤2-decimal validation) and `Park` singleton with `getInstance()` and `load(Path dataDir)`; verify with unit tests (same instance, reset on load)
- [x] 1.2 Add `SystemService` and the `SYSTEM DATE` / `SYSTEM PRICE` handling; verify valid and invalid inputs with unit tests

## 2. Groups

- [x] 2.1 Add `model.Group` (id, name, member list) and `GroupRepository`/`GroupFileStore` (`id;name;dni,…`); verify save/reload in a unit test
- [x] 2.2 Implement `GroupService` create/add/remove/delete with duplicate checks and ticket-derived lock; verify every user-groups scenario with unit tests

## 3. Tickets

- [x] 3.1 Add abstract `Ticket`, `IndividualTicket`, `GroupTicket` (composition) and `GroupDiscount`; verify prices and discount brackets with unit tests (6 × 40 → 192.00)
- [x] 3.2 Add `TicketRepository` (shared id space incl. components, active-ticket and group-ticket queries) and `TicketFileStore`; verify save/reload incl. a detached historical user
- [x] 3.3 Implement `TicketService` create/delete for individual and group tickets (date rules, derived-id conflicts, empty group, component deletion protection); verify every tickets scenario with unit tests

## 4. Attractions

- [x] 4.1 Add `model.Attraction` (atomic capacity-checked entry, exit) and `AttractionRepository`/`AttractionFileStore` (occupants not persisted); verify with unit tests
- [x] 4.2 Implement `AttractionService` create/enter/exit/list/status; verify individual, group, capacity, wrong-date and already-inside scenarios with unit tests

## 5. Users

- [x] 5.1 Extend `UserService.delete` with active-ticket and locked-group checks and removal from groups/attractions; verify the user-management deletion scenarios with unit tests and that Delivery 1 tests still pass

## 6. Commands and runtime

- [x] 6.1 Replace the command `switch` with a registry of commands (name, syntax, arity, handler) wired to `Park` services, with `HELP` generated from it; verify with unit tests that all 20 commands are dispatched and listed
- [x] 6.2 Add `--fly` handling and data-directory configuration in `App` (timestamped `tmp/` dir, recursive cleanup in `finally`); verify with a unit test that the directory is removed and normal-mode files are untouched
- [x] 6.3 Add `tmp/`, `groups.txt`, `tickets.txt`, `attractions.txt` to `.gitignore` and update `README.md` with new commands and `--fly`; verify `git status` stays clean after runs

## 7. Documentation and integration

- [x] 7.1 Write `uml/model.puml` (classes, enum, ticket hierarchy, composition, directed associations, multiplicities) consistent with the code; verify every model class appears and the file is well-formed `@startuml`/`@enduml`
- [x] 7.2 Run `mvn package` and the assignment's example sequence via the JAR in normal mode (twice, checking persistence) and with `--fly`; verify outputs match the specs and all tests pass
