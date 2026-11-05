## 1. Injected repositories

- [x] 1.1 Add `persistence.ParkRepositories`:
  - in-memory and file-backed factories;
  - getters, `save()` and `getMalformedLineCount()`.
- [x] 1.2 Change `ParkController.startExecution` to receive `ParkRepositories`, delegate `persist()` to it, and remove the repository creation from the controller.

## 2. Singleton controllers and wiring

- [x] 2.1 Convert `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `SystemManager` and `InvoiceManager` into Singletons:
  - private constructor and `getInstance()`;
  - `inject(...)` methods;
  - a `park()` accessor guarding uninitialized use.
- [x] 2.2 Add `business.ParkConfiguration.initialize(ParkRepositories)`: the composition root that injects repositories and collaborators, including `TicketManager` into `AttractionManager`.
- [x] 2.3 Make `ParkSession.open` build the repositories for the selected storage location (normal or `--fly`) and call `ParkConfiguration.initialize`.
- [x] 2.4 Make presentation handlers receive their manager by constructor, with `CommandProcessor` passing the configured singletons.

## 3. Documentation and verification

- [x] 3.1 Migrate the tests to `ParkConfiguration.initialize`, and add `ControllerConfigurationTest`:
  - singletons;
  - non-singleton repositories;
  - injection with two configurations;
  - no local creation;
  - uninitialized use;
  - no DI framework.
- [x] 3.2 Update `uml/model.puml` (stereotypes, `ParkConfiguration`, `ParkRepositories`, injection relationships) and README, and check them with PlantUML.
- [x] 3.3 Run `mvn package` and an end-to-end regression with the JAR in normal and `--fly` modes, comparing against the pre-change output.
