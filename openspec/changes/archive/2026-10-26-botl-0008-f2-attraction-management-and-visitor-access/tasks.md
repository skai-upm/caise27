# Tasks

## 1. Admission abstraction and attraction model

- [x] 1.1 Add `Admission` interface implemented by `Ticket` (`getAdmittedUsers`, `isValidOn`) with `IndividualTicket` and `GroupTicket` implementations; verify with tests on admitted users (detached owner → empty) and date validity
- [x] 1.2 Create `Attraction` with atomic `enter(Admission, LocalDate)`, `exit(Admission)`, capacity and availability; verify with `AttractionTest` (individual, group, wrong date, 3 places vs 4 users, exact capacity, already inside, exit frees places)

## 2. Management and persistence

- [x] 2.1 Create `AttractionStorage` (`Id;Nombre;Capacidad`) and `AttractionManager` (create/delete/find/enter/exit/removeUserEverywhere, positive integer capacity, empty-only deletion); verify with `AttractionManagerTest` including restart starting empty

## 3. Commands and model

- [x] 3.1 Add `TicketManager.findAdmission`, route `ATTRACTION CREATE|DELETE|ENTER|EXIT|STATUS` in `CommandProcessor` with the STATUS format, remove deleted users from attractions, update HELP and wire `data/attractions.txt` in `App`; verify with `CommandProcessorTest` running the proposal's T001/TG01 example and `mvn test`
- [x] 3.2 Update `docs/uml/model.puml` with `Admission`, its realisation by `Ticket`, `Attraction`, `AttractionManager`, `AttractionStorage` and multiplicities; verify every type appears and `plantuml -checkonly` passes
