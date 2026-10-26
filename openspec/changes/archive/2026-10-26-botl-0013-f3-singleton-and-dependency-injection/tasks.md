# Tasks

## 1. Repository abstractions

- [x] 1.1 Add `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` interfaces and make the storages implement them; verify `mvn compile` and storage tests pass

## 2. Singletons and injection

- [x] 2.1 Convert the four managers and `ParkController` to eager Singletons with fluent `configure(...)` injection (repositories typed by interfaces, `InvoiceDirector` injected, unconfigured use → `IllegalStateException`); verify with `SingletonTest` (same instance, private constructors, reconfiguration reloads, in-memory repository injection, injected director used)
- [x] 2.2 Move wiring to `App.configure(DataDirectory)` (remove `ParkController.load`) and adapt tests to `getInstance().configure(...)`; verify `mvn test` passes and the E1/E2 script output is identical in normal and `--fly` runs

## 3. Model

- [x] 3.1 Update `uml/model.puml` (repository interfaces and realisations, manager→repository associations typed by interfaces, singleton `instance` self-associations, ParkController→InvoiceDirector); verify `plantuml -checkonly` and the field cross-check pass
