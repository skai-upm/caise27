# Tasks

## 1. Model and rules

- [x] 1.1 Add `Pass`, `SpeedPass` and `SpecialPass`, and the pass registry in `ParkController` (purchase, Special Pass uniqueness, refunds, consumption, cascades in ticket and group-ticket deletion); verify unit tests for every `passes` scenario and the deletion cascades

## 2. Commands, invoices, persistence

- [x] 2.1 Add `PassCommands` (four commands), register them in `App`, and add them to `CommandCatalog`; verify command tests show prices and errors
- [x] 2.2 Include passes in user invoices; verify the personal-passes invoice scenario
- [x] 2.3 Add `PassRepository` to `ParkStorage`; verify a round trip including the consumed flag

## 3. Documentation and verification

- [x] 3.1 Update `uml/model.puml`; run `mvn -q package` and verify all tests pass
