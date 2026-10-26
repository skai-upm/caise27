# Tasks

## 1. Cascade deletion

- [x] 1.1 Add `clearPasses`/`restorePasses` to `IndividualTicket` and deletion listeners to `TicketController`; implement cascade for `delete` and `deleteGroupTicket` in the required order with validation first and rollback on storage failure; remove the interim "has passes" rejection; verify unit tests: ticket cascade, group cascade order, rejected deletion untouched, storage failure rolled back
- [x] 1.2 Register the pass-saving listener in `PassController.configure`; verify restart tests showing deleted passes are not restored and remaining passes keep price, ticket and consumed status

## 2. Persistence checks

- [x] 2.1 Verify end-to-end (`App.run` with and without `--fly`) that passes and attraction types are restored after restart in normal mode and never written to `data/` in fly mode; run `mvn test`

## 3. Model

- [x] 3.1 Reconcile `uml/model.puml` with the full E3 code; verify `plantuml -checkonly` and both cross-check directions (nothing missing, nothing invented)
