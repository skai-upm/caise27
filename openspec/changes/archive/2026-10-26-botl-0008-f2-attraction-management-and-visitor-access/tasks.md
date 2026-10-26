# Tasks

## 1. Attraction model

- [x] 1.1 Create `Attraction` with capacity, present users, atomic `enter(Ticket)` and `exit(Ticket)` using `Ticket.getUsers()`; verify unit tests for individual entry, group entry, full capacity rejection with no partial entry, already-present rejection and exit

## 2. Management, persistence and commands

- [x] 2.1 Create `AttractionRepository` (`Id;Nombre;Capacidad`) and `AttractionManager` (create with validation, delete only when empty, enter with date check, exit without date check, status text); verify unit tests including restart restoring attractions empty
- [x] 2.2 Add `ATTRACTION CREATE/DELETE/ENTER/EXIT/STATUS` to `CommandProcessor` and HELP, wire in `App`; verify a command test for the mixed individual/group status scenario and parameter errors, then run `mvn test`

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with `Attraction` (→ `0..*` `User`), `AttractionManager`, `AttractionRepository`; verify `plantuml -checkonly` succeeds
