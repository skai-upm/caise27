# Tasks

## 1. Attraction model and persistence

- [x] 1.1 Create `Attraction` (data, `available`, `canAdmit`, `admit`, `release`, `isEmpty`) and `AttractionRepository` (`<id>;<name>;<capacity>`, malformed lines skipped); verify with `AttractionTest` and `AttractionRepositoryTest`

## 2. Attraction operations

- [x] 2.1 Create `AttractionManager` (create/delete/enter/exit/status with all error responses, atomic capacity, date check, one attraction at a time, uniform handling through `Ticket.getVisitors()`); verify with `AttractionManagerTest` covering every attractions scenario including the 3-places/4-users case and restart
- [x] 2.2 Add the five ATTRACTION commands to `CommandProcessor` and HELP, wire `AttractionManager` with `data/attractions.txt` in `App` and the `Park` fixture; verify with `CommandProcessorTest`/`AppTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` with `Attraction`, `AttractionManager`, `AttractionRepository` and their associations; verify with `plantuml -checkonly`
- [x] 3.2 Verify end to end with `mvn package` and a command file reproducing the proposal example (T001 + TG01 enter, STATUS, T001 exits, STATUS, rejected group over capacity)
