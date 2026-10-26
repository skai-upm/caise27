# Tasks

## 1. Cascade deletion

- [x] 1.1 Separate deletion checks in `TicketController`, add `PassController.deletePassesOf`, replace `requireNoPasses` with cascade orchestration in `CommandProcessor`; verify with tests for individual and group cascades (including after restart) and for a rejected deletion leaving tickets and passes intact

## 2. Persistence verification

- [x] 2.1 Verify with tests that passes (type, price, consumed) and attraction types are restored after a restart, and with the jar that `--fly` writes passes only under `tmp/<timestamp>/`

## 3. Model

- [x] 3.1 Re-derive `uml/model.puml` for the E3 design and cross-check it with a script against all source fields; verify with `plantuml -checkonly`
- [x] 3.2 Run `mvn test` and the regression script; verify all tests pass and outputs are unchanged
