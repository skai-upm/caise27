# Tasks

## 1. Pass persistence

- [x] 1.1 Add `DataPaths.passes()`, `PassFileStore`, restoring pass constructors and file-backed `PassRepository`; wire it in `ApplicationInitializer` and save after purchases, refunds, cascades and consumption; verify with `PassPersistenceTest` (restart with prices/state, consumed pass not refundable, refunds/cascades, `--fly` leaves nothing)

## 2. UML

- [x] 2.1 Rewrite `uml/model.puml` for Delivery 3 and extend `UmlModelTest` to controllers and invoice classes; verify it passes

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C09] Extend delivery 3 persistence and UML`; verify with `git log -1 --format=%s`
