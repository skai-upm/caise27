# Tasks

## 1. Uniform ticket access

- [x] 1.1 Add abstract `getVisitors()` to `Ticket`, implemented by `IndividualTicket` (owner) and `GroupTicket` (constituents' owners); verify `IndividualTicketTest`/`GroupTicketTest` assertions on visitors

## 2. Attraction model and persistence

- [x] 2.1 Create `Attraction` with atomic `enter(Ticket, today)` (date, capacity, already-present checks), `exit(Ticket)`, `available()`; verify `AttractionTest` for individual/group entry, capacity boundary, wrong date, duplicate presence and exit
- [x] 2.2 Implement `AttractionRepository` (`Identificador;Nombre;Aforo`) and `AttractionService.create/find` with validation; verify `AttractionServiceTest` for rules and restart with empty occupancy

## 3. Commands

- [x] 3.1 Add `ATTRACTION CREATE/ENTER/EXIT/STATUS` and HELP lines to `CommandProcessor`, wire `attractions.txt` in `App`; verify `AttractionCommandsTest` with the source sequence (`ENTER T001`, `ENTER TG01`, `STATUS`, `EXIT T001`, `STATUS`) and the 3-places/4-users rejection

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` with `Attraction`, `AttractionService`, `AttractionRepository`, `getVisitors()` and associations/multiplicities; verify `UmlModelTest`, `plantuml -checkonly`, `mvn test` and `mvn package`
