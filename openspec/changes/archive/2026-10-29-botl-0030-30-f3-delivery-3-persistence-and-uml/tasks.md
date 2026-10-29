# Tasks

## 1. Persistence

- [x] 1.1 Store the purchase price in `Pass` and add `PassFileStorage`, `DataLocation.passes()` and the passes storage in `ParkStorage`; verify a round-trip unit test with consumed/unconsumed passes on standalone and composition tickets
- [x] 1.2 Load and save passes in `ParkController`, save after `ATTRACTION ENTER`, add `passes.txt` to `.gitignore`; verify restart tests (state, prices, single Special Pass, cascade, consumption saved) and that `--fly` leaves no permanent `passes.txt`

## 2. UML

- [x] 2.1 Update `uml/model.puml` with the Delivery 3 elements; verify `UmlModelConsistencyTest` (extended with the required declarations) passes

## 3. Verification and commit

- [x] 3.1 Run `mvn package` and a two-run JAR session buying and consuming passes; verify `passes.txt` and the reconstructed state
- [x] 3.2 Commit with the exact message `[F3-C09] Extend delivery 3 persistence and UML`; verify with `git log -1 --format=%s`
