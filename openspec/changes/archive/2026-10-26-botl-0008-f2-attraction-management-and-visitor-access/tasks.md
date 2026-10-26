# Tasks

## 1. Attraction model and persistence

- [x] 1.1 Create `Attraction` with polymorphic `admit(Ticket)`/`release(Ticket)`, capacity and duplicate checks done before any change; verify `AttractionTest` with individual and group tickets, exact-fit, overflow (nobody enters), already present, and partial release
- [x] 1.2 Create `AttractionRepository` (`id;name;capacity`) on a shared `TextFiles` line-file helper, also adopted by the existing repositories to avoid duplicated I/O code; verify `AttractionRepositoryTest` round-trip, malformed lines skipped, presence not stored

## 2. Attraction rules and commands

- [x] 2.1 Create `AttractionManager` (create with validation, enter with ticket-date check, exit without date check, persistence with reload on failure); verify `AttractionManagerTest` for every scenario of the spec including restart with empty attractions
- [x] 2.2 Add ATTRACTION CREATE/ENTER/EXIT/STATUS to `CommandProcessor` (listed by HELP, status formatting) and wire `App`/`TestPark` with `data/attractions.txt`; verify `CommandProcessorTest` reproduces the proposal's T001 + TG01 + EXIT status example

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` with `Attraction`, its `visitors` association and the new manager/repository; verify PlantUML `-checkonly`
- [x] 3.2 Run `mvn package` and an end-to-end JAR scenario (create attraction, individual and group entry, capacity rejection, exit, status, restart); verify outputs
