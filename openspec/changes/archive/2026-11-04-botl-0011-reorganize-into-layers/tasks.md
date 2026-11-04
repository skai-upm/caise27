# Tasks

## 1. Package move

- [x] 1.1 Move model, service, persistence and presentation classes (and their tests) into the new packages with updated package/import declarations and the required visibility changes; update the JAR manifest main class; verify `mvn test` passes with no behaviour change
- [x] 1.2 Move `DataFiles` to persistence and add `ParkSession` (permanent/temporary with cleanup) so `App` no longer imports persistence; verify the `--fly` and normal-mode `AppTest` cases pass

## 2. Controller split

- [x] 2.1 Extract `ParkController` (managers, date, price, cross-cutting rules) from `CommandProcessor`, leaving parsing/formatting in presentation; verify all `CommandProcessorTest` cases pass unchanged in outcome and add `ParkControllerTest` for the moved rules (deletion with tickets, group lock, price positivity)

## 3. Architecture checks and model

- [x] 3.1 Add `LayeringTest` (no class outside the four packages, presentation imports no persistence, model imports no file I/O/services/persistence, persistence imports no presentation/services) and make `UmlModelTest` package-aware; verify both pass
- [x] 3.2 Regenerate `uml/model.puml` with the packages and new classes; verify with the PlantUML `-syntax` check

## 4. Regression

- [x] 4.1 Run `mvn clean package`, then run the packaged JAR against data files written by the previous version and confirm identical outputs
