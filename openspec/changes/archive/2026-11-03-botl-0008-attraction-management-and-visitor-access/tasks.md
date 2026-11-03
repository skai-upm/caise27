# Tasks

## 1. Polymorphic ticket access

- [x] 1.1 Add abstract `getVisitors()` to `ParkTicket`, implemented by `Ticket` (owner) and `GroupTicket` (constituent owners), and `TicketService.findUsable(id)`; verify tests for both kinds and that constituent ids are not usable

## 2. Attraction model and catalog

- [x] 2.1 Create `Attraction` (id, name, capacity, present users; atomic `enter(ParkTicket, today)`, `exit`, available places); verify `AttractionTest` for valid entry, wrong date, capacity boundary (exactly full / one too many), already present and exit
- [x] 2.2 Create `AttractionFileStorage` (`Id;Nombre;Capacidad`) and `AttractionService` (create with validation, find, enter, exit); verify `AttractionServiceTest` for invalid creations, file format and reload with empty attractions

## 3. Commands

- [x] 3.1 Add `ATTRACTION CREATE/ENTER/EXIT/STATUS` to `CommandProcessor` (EXIT by ticket id or email of a present user; STATUS format from design) and HELP; wire in `App`; add `attractions.txt` to `.gitignore`; verify `AttractionCommandsTest` with the source sequence (T001 + TG01, status, exit T001, status), the 3-places/4-users rejection and the 19 HELP entries

## 4. UML

- [x] 4.1 Update `uml/model.puml` with `Attraction`, `AttractionService`, `AttractionFileStorage`, `getVisitors()` and relationships; verify `plantuml -checkonly`

## 5. Integration verification

- [x] 5.1 Verify an `AppTest` file-mode session with attractions, a restart showing empty attractions, and `mvn clean test` passing
