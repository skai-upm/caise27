# Tasks

## 1. Repository abstractions and injection

- [x] 1.1 Add `business.repository` interfaces and `ParkRepositories`; rename persistence classes to `Text*Repository` implementing them and add `TextRepositories.inDirectory`; verify compilation and persistence tests
- [x] 1.2 Move state loading/saving into `ParkController` (`configure`, `loadState`, `saveState`) and remove `ParkStorage`; verify with persistence round-trip tests and an in-memory repository injection test

## 2. Controller collaboration

- [x] 2.1 Add `InvoiceController` singleton with injected `ParkController` and move invoice creation to it; verify with invoice tests
- [x] 2.2 Inject controllers into `CommandProcessor` and commands; wire everything in `App` (normal and `--fly` differ only in the directory); verify with command and integration tests and `ArchitectureTest`

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR in normal and `--fly` mode with a command file; verify unchanged behaviour
- [x] 3.2 Commit with the exact message `[F3-C03] Add singleton controllers and dependency injection`; verify with `git log -1 --format=%s`
