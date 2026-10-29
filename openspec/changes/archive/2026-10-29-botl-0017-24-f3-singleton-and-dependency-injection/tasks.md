# Tasks

## 1. Repository abstractions and injection

- [x] 1.1 Add `business.repository` interfaces and `Repositories`; rename persistence repositories to `TextFile*Repository` implementing them; add `TextFileRepositories.create(Path)` and remove `ParkStorage`; verify `mvn -q compile` and the architecture test (business does not import persistence)
- [x] 1.2 Add `configure(Repositories)`, `load()` and `persist()` to `ParkController` (reset clears configuration); verify with tests: round trip through text-file repositories for two directories, and through an in-memory test implementation

## 2. Controllers and collaborations

- [x] 2.1 Add `InvoiceController` singleton with injected `ParkController` and `InvoiceDirector`, move invoice operations there; inject controllers into `CommandProcessor`; verify with tests for same-instance access, no public constructor, and invoice output unchanged
- [x] 2.2 Rewire `App` as composition root (create repositories for the chosen directory, inject, load, persist after each command); verify all `AppTest`/integration tests pass unchanged (normal, restart, `--fly`)
- [x] 2.3 Update `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run in normal and `--fly` mode
