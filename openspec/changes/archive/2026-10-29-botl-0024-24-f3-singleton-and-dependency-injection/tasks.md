# Tasks

## 1. Singletons and injection

- [x] 1.1 Turn `UserService`, `GroupService`, `TicketService`, `AttractionService`, `InvoiceService` into singletons with `configure(...)` injection; update tests to use `getInstance().configure(...)`; verify a test that each has one instance and no accessible constructor
- [x] 1.2 Add `ParkDependencies`, `persistence.file.ParkStorage` and the composition root `ParkInitializer`; make `ParkController` receive its dependencies from it instead of creating them; verify a test that two initializations inject different repository instances into the same controllers, and all existing tests pass
- [x] 1.3 Run `mvn package` and the JAR in normal and `--fly` modes; verify unchanged behavior

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F3-C03] Add singleton controllers and dependency injection`; verify with `git log -1 --format=%s`
