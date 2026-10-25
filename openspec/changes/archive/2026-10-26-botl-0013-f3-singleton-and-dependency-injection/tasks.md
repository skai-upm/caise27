# Tasks

## 1. Repository abstractions

- [x] 1.1 Turn the five repositories into interfaces and move their code into `TextFile…Repository` implementations; update all users; verify `mvn test` passes

## 2. Singleton controllers and startup wiring

- [x] 2.1 Make the five controllers Singletons (`initialize(deps…)`, `getInstance()`, private constructor) and update their creation sites; verify `mvn test` passes
- [x] 2.2 Add `Bootstrap.configure(dataDir, today)` as composition root, reduce `App.start` to argument handling and delegation, and build `Park` on it; verify with `SingletonTest` (identity, processor uses the singletons, reinitialisation) and `AppTest` (normal and `--fly`)
- [x] 2.3 Add an in-memory `UserRepository` in test code and verify `UserManager` works with it unchanged

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` (interfaces, realisations, `Bootstrap`, usages); verify `plantuml -checkonly` and `UmlModelTest`
- [x] 3.2 Rerun the Delivery-2 baseline command file (normal and `--fly`) with the new JAR and verify identical output
