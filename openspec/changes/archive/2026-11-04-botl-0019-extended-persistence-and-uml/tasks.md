# Tasks

## 1. Cascade deletion

- [x] 1.1 Add `TicketManager.requireDeletable/requireGroupDeletable` and `PassManager.removeFor/restore`; verify with unit tests (validation without mutation, restore keeps order and file content)
- [x] 1.2 Implement cascade + compensation in `ParkController.deleteTicket/deleteGroupTicket`; verify with `ParkControllerTest` (passes removed with ticket and group ticket, rejected deletions keep passes, injected storage failures leave memory and files unchanged)

## 2. Persistence audit

- [x] 2.1 Add restart tests for pass reconstruction (kind, price, consumed, constituent linkage usable at SPECIAL entry) and typed attractions, and a `--fly` test for passes/typed attractions; verify all pass

## 3. UML and regression

- [x] 3.1 Review `uml/model.puml` against the code; verify `UmlModelTest` and the PlantUML `-syntax` check
- [x] 3.2 Run `mvn clean package` and the E1/E2 regression script (no new differences)
