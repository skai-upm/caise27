# Tasks

## 1. Cascade and atomicity

- [x] 1.1 Add `IndividualTicket.removePasses`, `GroupTicket.removeComponents`/`restoreComponents` and make `TicketManager` deletions validate-cascade-save with undo on save failure; verify with `TicketManagerTest` (cascade order effects, rejected deletion unchanged, failing repository leaves everything in place)

## 2. Persistence verification

- [x] 2.1 Add restart tests for pass prices/statuses (standalone and internal tickets) and attraction types, and an `AppTest` proving `--fly` keeps passes/attractions out of `data/`; verify `mvn test`

## 3. Model

- [x] 3.1 Review `uml/model.puml` against the E3 code and complete it; verify `plantuml -checkonly` and the field cross-check pass
