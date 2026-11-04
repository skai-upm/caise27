# Tasks

## 1. Baseline

- [x] 1.1 Build the current JAR and record a reference run of a fixed E1/E2 script (users, groups, tickets, group tickets, attractions, date, price, help, errors) in a scratch directory: output plus all data files; verify the reference files exist

## 2. Package move

- [x] 2.1 Move production classes into `presentation`, `business.model`, `business.service` and `persistence` (and tests into the matching test packages), rewriting `package` and `import` declarations and widening only the members needed across packages; verify `mvn clean test` passes with every existing test

## 3. Controller extraction

- [x] 3.1 Create `business.controller.ParkController` with the system date, ticket price and the cross-entity operations moved verbatim from `CommandProcessor`; reduce `CommandProcessor` to parsing, dispatch and formatting over the controller; update `App` wiring and tests; verify `mvn clean test` passes
- [x] 3.2 Add `LayeringTest` (package declarations, no persistence or file I/O in presentation and model, model independent of other layers, presentation used only by `App`) and make `UmlModelTest` scan recursively; verify both pass

## 4. Regression and model

- [x] 4.1 Run the baseline script with the new JAR in a fresh directory and in a copy of the baseline data directory; verify output and data files are byte-identical to the reference
- [x] 4.2 Update `uml/model.puml` with package blocks and `ParkController` (`controller` association from `CommandProcessor`, manager associations, moved attributes); verify PlantUML `-syntax` and the label/field check
- [x] 4.3 Run `mvn clean package` and `java -jar` on the built JAR; verify success
