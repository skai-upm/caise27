# Tasks

## 1. HELP

- [x] 1.1 Add a verbatim assertion of every Delivery 1–3 HELP line and an extended-help purpose check; verify it passes

## 2. Integration and restrictions

- [x] 2.1 Add `Delivery3IntegrationTest` (full phase-3 flow through the real wiring, restart, `--fly` file without leftovers); verify it passes
- [x] 2.2 Add `Delivery3RestrictionsTest`; verify it passes

## 3. Verification

- [x] 3.1 Run `mvn -q package`, then the JAR with `input_fase3.txt` normally and with `--fly`; verify all tests pass and `tmp/` is empty
