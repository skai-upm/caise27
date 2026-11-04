# Tasks

## 1. Polymorphic ticket holders

- [x] 1.1 Add abstract `getHolders()` to `Ticket`, implement it in `IndividualTicket` and `GroupTicket`, and add `TicketManager.findForAccess`; verify with `GroupTicketTest`/`TicketManagerTest` cases for holders and lookup (constituent ids not accepted)

## 2. Attractions

- [x] 2.1 Implement `Attraction` (capacity, present users, all-or-nothing `enter`, `exit`, available places); verify with `AttractionTest` (individual and group entry, date mismatch, capacity boundary and overflow, already present, exit without date check)
- [x] 2.2 Implement `AttractionFileStore` and `AttractionManager` (create with validation, persistence of definitions, status text, `removeUserEverywhere`); verify with `AttractionManagerTest` (invalid creations, file content, restart empty, status content)

## 3. Commands and wiring

- [x] 3.1 Dispatch `ATTRACTION CREATE|ENTER|EXIT|STATUS` in `CommandProcessor`, remove deleted users from attractions, add HELP lines and wire `attractions.txt` in `App`; verify with `CommandProcessorTest`/`AppTest` (source example sequence with T001 and TG01, exit then status, 3-places/4-users rejection, restart empties)

## 4. Model and regression

- [x] 4.1 Update `uml/model.puml` with attraction classes, `getHolders()` and multiplicities; verify with PlantUML `-checkonly`
- [x] 4.2 Verify `mvn test` passes and run the packaged JAR with the source's ENTER/STATUS/EXIT example
