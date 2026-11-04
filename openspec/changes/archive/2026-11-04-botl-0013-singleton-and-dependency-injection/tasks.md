# Tasks

## 1. Singleton controller with injected collaborators

- [x] 1.1 Make `ParkController` a Singleton (private constructors, `initialize` with managers + `InvoiceDirector` [+ startup date], `getInstance`, `shutdown`) and use the injected `invoiceDirector` in the invoice operations; verify with `ParkControllerTest` (same instance, second `initialize` refused, `getInstance` before init fails, new instance after `shutdown`)
- [x] 1.2 Initialise the controller in `App.runWith` with the startup-created services and director and shut it down in `finally`; update `CommandProcessorTest`, `CommandRunnerTest` and `ParkControllerTest` to initialise/shutdown around each test; verify `mvn clean test` passes, including the back-to-back normal and `--fly` runs in `AppTest`

## 2. Replaceable repositories

- [x] 2.1 Add a `UserFileRepositoryTest` case with two repository instances on different files coexisting; verify no production class other than `App` constructs a repository, service or controller (`grep` over `src/main`)

## 3. Model and integration

- [x] 3.1 Update `uml/model.puml` (`ParkController` `<<Singleton>>`, `instance` self-association, `invoiceDirector` association replacing the usage arrow); verify PlantUML `-syntax`, `UmlModelTest` and the label/field check
- [x] 3.2 Run `mvn clean package` and the JAR in normal and `--fly` modes with the same commands, confirming identical output
