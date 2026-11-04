# Tasks

## 1. Polymorphic visitors

- [x] 1.1 Add abstract `Ticket.getVisitors()` implemented by `IndividualTicket` (its user if present) and `GroupTicket` (users of its parts), and `TicketManager.findAnyTicket(id)` (individual, group, constituent); verify with new cases in `IndividualTicketTest`, `GroupTicketTest` and `TicketManagerTest`

## 2. Attractions

- [x] 2.1 Create `Attraction` (id, name, capacity, identity-based present list; atomic `enter(Ticket, today)`, `exit(Ticket)`, `getAvailable()`); verify with `AttractionTest` (individual and group entry, wrong date, capacity boundary 3 free vs 3/4 users, already present, exit frees places, exit without date check)
- [x] 2.2 Create `AttractionFileRepository` and `AttractionManager` (create with regex-free capacity validation and rollback, find, `removeUserEverywhere`, definitions only); verify with `AttractionManagerTest` (invalid creations, restart restores empty attractions, exact file line)

## 3. Commands and wiring

- [x] 3.1 Add `ATTRACTION CREATE/ENTER/EXIT/STATUS` to `CommandProcessor` and `HELP_LINES`, remove deleted users from attractions, wire `attractions.txt` in `App.run` and `.gitignore`; verify with `CommandProcessorTest`/`AppTest` (source example sequence with T001 and TG01, STATUS contents after entry and exit, parameter errors, restart empties attractions)

## 4. Model and integration

- [x] 4.1 Update `docs/uml/model.puml` (attraction classes, present-users association, `getVisitors()`); verify all production classes appear and PlantUML `-syntax` accepts it
- [x] 4.2 Run `mvn clean test` and run the packaged JAR with the source example
