# Tasks

## 1. Singleton controllers and injection

- [x] 1.1 Convert the five services to singletons with `configure(...)` injection (no `ParkController.getInstance()` or repository lookups inside operations), add `persistence.ParkRepositories`, and make `ParkController.initialize`/`load` inject repositories and wire the controllers; verify the project compiles
- [x] 1.2 Update `CommandProcessor`, `App` and all tests to the singletons (failing-repository injection for the unexpected-error test); verify all tests pass

## 2. Verification

- [x] 2.1 Add `DependencyInjectionTest` (one instance per controller, private constructors, repositories without static instance and with public constructors, controllers operate on injected in-memory repositories, two configurations with different paths) and update `uml/model.puml`; verify with `mvn package` and the PlantUML check
- [x] 2.2 Re-run the Delivery 2 baseline session (normal and `--fly`) with the new JAR and verify identical output and data files
