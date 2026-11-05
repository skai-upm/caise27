# Tasks

## 1. Stored pass prices

- [x] 1.1 Store the price in `Pass` at purchase, persist it in `PassRepository` (5-field lines, 4-field compatible), and add a restore that accepts the stored price; verify persistence tests for stored prices, consumed state, legacy lines and persisted cascades

## 2. UML

- [x] 2.1 Add the pass cardinalities to `uml/model.puml` and extend `UmlModelTest` with the Delivery 3 checklist; verify it passes

## 3. Verification

- [x] 3.1 Run `mvn -q package` and a `--fly` JAR run with passes; verify all tests pass and nothing remains in `tmp/`
