# Tasks

## 1. Polymorphic ticket holders

- [x] 1.1 Add abstract `getHolders()` to `Ticket` with implementations in `IndividualTicket` and `GroupTicket`, and `TicketManager.findAnyTicket`; verify with unit tests in `GroupTicketTest`/`TicketManagerTest`

## 2. Attractions

- [x] 2.1 Create `Attraction` with uniform `enter(Ticket, today)` (date, already present, capacity, atomic) and `exit(Ticket)`; verify with `AttractionTest` (individual, group, wrong date, 3 places vs 4 users, exactly full, duplicate entry, exits)
- [x] 2.2 Create `AttractionFileStorage` and `AttractionManager` (create with validation, definitions persisted, empty occupancy on load); verify with `AttractionManagerTest`

## 3. Commands

- [x] 3.1 Add `ATTRACTION CREATE/ENTER/EXIT/STATUS` to `CommandProcessor` with the STATUS format, HELP entries and `attractions.txt` wiring in `App`; verify with `CommandProcessorTest` (source example sequence: T001 + TG01, status, exit, status) and an `AppTest` restart (definition kept, empty)

## 4. Model and regression

- [x] 4.1 Update `docs/uml/model.puml` (attraction classes, present users, `getHolders()`); verify with the PlantUML `-syntax` check and class existence
- [x] 4.2 Run `mvn clean package` with all tests passing and a manual JAR run of the source example
