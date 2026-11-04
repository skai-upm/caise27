# Tasks

## 1. Ordered, failure-safe cascades

- [x] 1.1 Add side-effect-free `checkDeletable`/`checkGroupDeletable` to `TicketService` and `removePassesOf` (returning removed) / `restore` to `PassService`; make `ParkController` delete passes first, then tickets (group: constituents then group ticket), restoring passes if ticket deletion fails; verify `CascadeDeletionTest` (individual and group cascades, validation failure keeps everything, simulated write failure leaves no partial deletion)

## 2. Persistence and --fly evidence

- [x] 2.1 Verify with `F3PersistenceTest`: restart keeps pass id, price, ticket, consumed status and attraction types (incl. legacy line), and a `--fly` run with F3 data leaves permanent files untouched and no `tmp/`

## 3. UML and regression

- [x] 3.1 Check `uml/model.puml` against the implemented F3 design (add a `UmlModelTest` assertion for the F3 elements), verify `plantuml -checkonly`, `mvn package`, and the F3 regression script identical to the previous JAR
