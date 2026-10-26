# Tasks

## 1. Preparation inside the current package

- [x] 1.1 Move date/price parsing and formatting from `MainController` to a new `Values` class and update callers and tests; verify `mvn test` passes
- [x] 1.2 Rename `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager` (and their fields/tests) to `UserController`, `GroupController`, `TicketController`, `AttractionController`; verify `mvn test` passes

## 2. Layer packages

- [x] 2.1 Move production classes to `presentation`, `business` (`model`, `controller`, `validation`), `persistence`, keeping `App` in `es.upm.parque`; regenerate package declarations and explicit imports; adjust visibility (`GroupTicket.addTicket/removeTicket`); verify `mvn compile` succeeds
- [x] 2.2 Move tests to mirrored packages with a public `TestPark` fixture; verify all existing tests pass unchanged in behaviour

## 3. Architecture checks, model and end-to-end

- [x] 3.1 Add `LayeredArchitectureTest` (packages exist, no default package, presentation and model free of persistence/file-access imports); verify it passes
- [x] 3.2 Update `uml/model.puml` with the layer packages; verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.3 Run `mvn package` and an end-to-end JAR run covering E1 and E2 commands (users, HELP, groups, tickets, group tickets, attractions, `--fly`); verify responses match the behaviour before the reorganisation
