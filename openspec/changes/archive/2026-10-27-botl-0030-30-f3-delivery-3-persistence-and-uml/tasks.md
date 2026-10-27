# Tasks

## 1. Pass persistence

- [x] 1.1 Add pass restore constructors, `PassFileStorage`, storage support in `PassRepository`, and wire it into `ParkRepositories` and `ParkController.persistAll`; verify with `ParkStorageTest` (round trip of SpeedPasses and Special Passes with price and consumed state, unresolved ticket skipped)
- [x] 1.2 Verify with `PersistenceIntegrationTest` that passes and consumed states survive a restart, refunds and cascade deletions are not restored, stored prices are kept, and `--fly` leaves no data

## 2. Final UML

- [x] 2.1 Update `uml/model.puml` to the final Delivery 3 design and extend `UmlModelTest` with the Delivery 3 checks; verify with the PlantUML syntax check and `mvn package`
