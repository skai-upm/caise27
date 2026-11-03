# Tasks

## 1. Cascading deletion

- [x] 1.1 Add `requireDeletable`/`requireGroupDeletable` to `TicketController`, `removeFor`/`restore` to `PassController`, and cascade orchestration in `ParkController` for `TICKET DELETE` and `GROUPTICKET DELETE`; verify `CascadeDeletionTest` (individual and group cascades, rejected deletions keep everything, a failing ticket repository restores the passes)

## 2. Persistence and isolation checks

- [x] 2.1 Verify with tests that a consumed pass and attraction types survive a restart, that no orphan pass remains after cascades and restart, and that a `--fly` run with passes and typed attractions leaves permanent files untouched and removes its directory

## 3. UML

- [x] 3.1 Check `uml/model.puml` field by field against the implementation (script) and with `plantuml -checkonly`; then `mvn clean test` and the regression script
