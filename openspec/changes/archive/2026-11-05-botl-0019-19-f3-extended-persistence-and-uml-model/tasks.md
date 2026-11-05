# Tasks

## 1. Cascade deletion

- [x] 1.1 Add `TicketManager.checkDeletable/checkGroupDeletable`, `PassManager.removePassesOf/restore`, and cascade orchestration in `ParkController.deleteTicket/deleteGroupTicket`; verify with `CascadeDeletionTest` (individual cascade incl. restart, group cascade, rejected deletion keeps passes, failing ticket store restores passes and ticket using a failing repository double)

## 2. Persistence contract and isolation

- [x] 2.1 Verify restart restores consumed passes, prices, ticket links and attraction types through `ApplicationContext`, and that a `--fly` run with passes and typed attractions leaves normal files byte-identical and removes its directory (`AppTest`/`ApplicationContextTest`)

## 3. Regression and UML

- [x] 3.1 Run `mvn test`, diff the E1/E2 script output with the previous JAR (no differences expected), run the layer check, and verify/complete `uml/model.puml` (PlantUML `-checkonly` plus cross-check)
