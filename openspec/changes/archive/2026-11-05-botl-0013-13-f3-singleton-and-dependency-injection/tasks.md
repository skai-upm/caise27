# Tasks

## 1. Repository abstractions

- [x] 1.1 Add `UserRepository`, `GroupRepository`, `TicketRepository`, `GroupTicketRepository`, `AttractionRepository` interfaces and make the file stores implement them; verify `mvn test` passes

## 2. Singleton controllers with injection

- [x] 2.1 Convert `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager` to singletons with private constructors, `getInstance()` and static `configure(...)` taking repository interfaces and collaborating managers; adapt tests; verify `mvn test` passes and a `SingletonTest` checks instance identity and private constructors
- [x] 2.2 Convert `ParkController` to a singleton configured with the managers, an injected `InvoiceDirector` and the date; remove its store construction and temporary-directory handling; verify invoice and controller tests pass

## 3. Startup wiring

- [x] 3.1 Add `ApplicationContext` (`start`, `startTemporary`, `close`) as the only place creating repositories and calling `configure`, and switch `App` to it; verify with `ApplicationContextTest` (normal vs --fly repository paths with the same controller instances, temp dir removed on close) and `AppTest`
- [x] 3.2 Verify substitution with an in-memory `UserRepository` test double (no file written), check by script that no business operation constructs repositories/managers/controller/director outside `ApplicationContext`, run the layer check and the E1/E2 equivalence script against the previous JAR

## 4. Documentation

- [x] 4.1 Update `uml/model.puml` (repository interfaces and realizations, singleton controllers, `ApplicationContext`); verify PlantUML `-checkonly` and the code cross-check
