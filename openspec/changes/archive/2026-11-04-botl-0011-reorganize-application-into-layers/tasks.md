# Tasks

## 1. Baseline

- [x] 1.1 Build the current JAR and record the output and data files of an E1/E2 command script (users, groups, date, price, tickets, group tickets, attractions, HELP, EXIT) as the behavioural baseline; verify the baseline files exist in the scratch directory

## 2. Split controller and runner

- [x] 2.1 Extract `ParkController` (date, price, services, cross-entity rules, `open(dataDir)`) from `CommandProcessor` and `CommandRunner` (input loop) from `App`, and `DataDirectory` from `App`; verify `mvn test` passes before moving packages

## 3. Move into layered packages

- [x] 3.1 Move production classes into `presentation`, `business.model`, `business.service`, `business.controller`, `persistence` (App stays in `com.parque`), fix package declarations, imports and required visibility; verify `mvn test` passes
- [x] 3.2 Move tests to the packages of the classes they test and add `LayeringTest` enforcing package membership and allowed dependencies (presentation ↛ persistence, model ↛ persistence/presentation/files, business ↛ presentation); verify `mvn test` passes

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` with packages and the new classes (`ParkController`, `CommandRunner`, `DataDirectory`) and make `UmlModelTest` resolve classes in their packages; verify `plantuml -checkonly` and `UmlModelTest`
- [x] 4.2 Run the baseline script with the new JAR and compare output and data files byte-for-byte with the baseline; verify `mvn package` builds the executable JAR
