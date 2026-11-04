# Tasks

## 1. Singleton controller

- [x] 1.1 Make `ParkController` a per-execution Singleton (private constructor, `initialize`, `getInstance`, `release`), inject `InvoiceDirector`, remove `open`; verify `ComponentWiringTest` (same instance, second initialize rejected, access before init rejected, new execution gets fresh date/price, private constructor)

## 2. Composition root and injection

- [x] 2.1 Create `ApplicationContext.start(dataDir)/stop()` creating repositories, services and director and initializing the controller; use it from `App` (start/stop per run) and make `CommandProcessor()` use `getInstance()`; verify `ComponentWiringTest` that two starts on different directories use their own files and that normal and `--fly` runs (`FlyModeTest`, `AppTest`) still pass

## 3. Tests and architecture rules

- [x] 3.1 Add `TestPark` helper and migrate tests away from removed constructors/`open`; extend `LayeringTest` (root package allows `ApplicationContext`; controller must not depend on persistence); verify full `mvn test`

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` (`<<Singleton>>` `ParkController` with `instance` self-association, injected `invoiceDirector`, `ApplicationContext`) and verify `UmlModelTest`/`plantuml -checkonly`; verify the E1/E2/F3 script gives byte-identical output and data files compared with the JAR before this change, and `mvn package` succeeds
