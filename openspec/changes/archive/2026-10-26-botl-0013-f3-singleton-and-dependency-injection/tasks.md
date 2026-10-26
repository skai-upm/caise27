# Tasks

## 1. Singleton controllers

- [x] 1.1 Convert `MainController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController` to holder-based singletons with private constructors, `configure(...)` injection of repositories/collaborators, and a not-configured guard; verify `mvn compile`

## 2. Initialisation

- [x] 2.1 Replace `App.createProcessor` with `App.configure(dataDir, clock)` creating repositories and configuring controllers in dependency order; make `TestPark` and the controller tests use `getInstance()`/`configure`; verify the whole existing suite passes
- [x] 2.2 Add `ComponentConfigurationTest`: same instance from repeated `getInstance()`, no public constructors (reflection), unconfigured controller error, repositories are distinct non-singleton objects, and re-configuration with another directory (fly-style) switches data without code changes; verify it passes

## 3. Model and end-to-end

- [x] 3.1 Update `uml/model.puml` (controller associations unchanged, document singletons in a note); verify PlantUML `-checkonly` and the UML cross-check script
- [x] 3.2 Run `mvn package` and the regression script in normal and `--fly` mode; verify output identical to the previous delivery's
