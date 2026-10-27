# Tasks

## 1. Package reorganisation

- [x] 1.1 Capture the Delivery 2 session output and data files with the current JAR as a baseline, then move every production and test class to its layer package (package declarations, imports, visibility, `Main-Class`); verify `mvn package` passes

## 2. Responsibilities and exceptions

- [x] 2.1 Move data loading to `ParkController.load` and `--fly` directory handling to `persistence.DataDirectories`; introduce `ParkException`, `PersistenceException` and `InvalidCommandException` and rewrite `CommandProcessor` error handling around them; verify all tests pass
- [x] 2.2 Add `ArchitectureTest` (layer packages, presentation not using persistence, model not using persistence, exception hierarchy) and verify with `mvn package`
- [x] 2.3 Update `uml/model.puml` to the layered packages and the new exception/persistence classes; verify `UmlModelTest` and the PlantUML syntax check pass
- [x] 2.4 Re-run the baseline session with the new JAR (normal and `--fly`) and verify identical output and data files
