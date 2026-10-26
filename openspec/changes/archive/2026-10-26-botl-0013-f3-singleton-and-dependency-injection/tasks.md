# Tasks

## 1. Singleton controllers

- [x] 1.1 Convert `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController` to eager singletons with `getInstance()`, `configure(...)` (inject + reload, returns `this`) and a not-configured guard; verify `mvn compile`
- [x] 1.2 Inject all dependencies from `App.buildProcessor` in dependency order with repositories created there; verify the regression script output is unchanged in normal and `--fly` modes

## 2. Tests

- [x] 2.1 Update tests to `getInstance().configure(...)` and add tests for instance uniqueness and the not-configured guard; verify `mvn test` passes

## 3. Model

- [x] 3.1 Mark the singleton controllers in `uml/model.puml`; verify with `plantuml -checkonly`
