# Tasks

## 1. Repository abstractions

- [x] 1.1 Create `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` interfaces in `business.repository`; rename persistence classes to `File*Repository` implementing them; verify `mvn compile` and that no business source imports the persistence package

## 2. Singleton controllers with injection

- [x] 2.1 Convert the five controllers to eager Singletons with `getInstance()` and `configure(...)` (reload data, reset checks, register ticket checks on injected controllers); verify a test that `getInstance()` returns the same object, constructors are private and reconfiguration replaces state
- [x] 2.2 Make `App` create the file repositories for the current mode (normal or `--fly`) and configure the controllers in dependency order; verify the existing `AppTest` normal/fly scenarios pass
- [x] 2.3 Update all tests to configure the singletons on temporary directories and extend `ArchitectureTest` with the business → persistence rule; verify `mvn test` passes

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with `<<Singleton>>` controllers, repository interfaces in `business.repository` realised by `File*Repository`; verify `plantuml -checkonly` and the model/code cross-check pass
