# Tasks

## 1. Package reorganization

- [x] 1.1 Capture the pre-change JAR output of the Delivery 2 command file as a baseline; move classes with `git mv` into `presentation`, `business`, `business.model` and `persistence`, update package/import declarations, `pom.xml` main class and test locations; verify `mvn package` passes

## 2. Exceptions and business operations

- [x] 2.1 Add `ParkException`, `NotFoundException`, `InvalidDataException`, `RuleViolationException` (business) and `InvalidCommandException` (presentation)
- [x] 2.2 Move user, system, group, ticket and attraction rules into business managers throwing those exceptions with the former messages; make presentation command classes parse, call managers, format `OK` responses, and `CommandProcessor` catch the exceptions; verify the full suite passes unchanged in behavior
- [x] 2.3 Add `ParkSession` in business and make `App` use it so presentation imports nothing from persistence; verify with `LayeredArchitectureTest` (packages, no default package, presentation→persistence and model→persistence/nio forbidden) and an exception-flow test (duplicate DNI rule violation message, processing continues)

## 3. Documentation and regression

- [x] 3.1 Update `uml/model.puml` and `UmlModelConsistencyTest` to the new packages/classes; verify PlantUML `-checkonly` and `mvn test`
- [x] 3.2 Rebuild the JAR and compare the Delivery 2 command file output with the baseline (identical); verify `--fly` still leaves no data
