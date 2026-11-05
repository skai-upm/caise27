# Tasks

## 1. Port and implementation

- [x] 1.1 Add `ParkRepository` and `RepositoryException` to business, and make `ParkStorage` implement the port with constructor-injected repositories plus `inDirectory`; verify the persistence tests pass using the factory

## 2. Injection

- [x] 2.1 Add `ParkController.setRepository/loadState/saveState`, wire it in `App` (composition root), and verify `App` and integration tests pass
- [x] 2.2 Add `DependencyInjectionTest` (in-memory repository round trip, `NONE` default, distinct storages, singleton checks); verify it passes

## 3. Documentation and verification

- [x] 3.1 Update `uml/model.puml` with the port, exception and injection relationships; run `mvn -q package` and verify all tests pass and the JAR behaves the same in normal and `--fly` modes
