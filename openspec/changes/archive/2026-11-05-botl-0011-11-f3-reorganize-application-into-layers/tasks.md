# Tasks

## 1. Baseline

- [x] 1.1 Build the pre-refactor JAR and record outputs and data files of a fixed E1/E2 command script on seed data (baseline for equivalence); verify the baseline files exist

## 2. Package moves

- [x] 2.1 Move model classes to `business.model`, stores to `persistence` (with `DataDirectory` and `Attraction.parseCapacity`), managers/validator/`Result` to `business`, `App`/`CommandRunner`/`CommandProcessor` to `presentation`, updating packages, imports and visibility; move tests to matching packages; verify `mvn test` passes
- [x] 2.2 Extract `ParkController` (date, price, cross-entity rules, composition root, temporary-directory lifecycle) from `CommandProcessor` and `App`; verify `mvn test` passes with tests adapted to the controller and a new `ParkControllerTest` for `open`/`openTemporary`/`close`

## 3. Boundaries and build

- [x] 3.1 Update the manifest main class and verify `mvn package` produces a runnable JAR; check layer rules with a script (no default package, import directions, no file I/O in presentation or model)
- [x] 3.2 Run the baseline script with the new JAR on a copy of the seed data and verify output and data files are identical to the baseline

## 4. Documentation

- [x] 4.1 Update `uml/model.puml` to the layered packages and new classes; verify PlantUML `-checkonly` and the code cross-check script pass
