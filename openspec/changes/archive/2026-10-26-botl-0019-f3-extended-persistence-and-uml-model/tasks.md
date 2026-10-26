# Tasks

## 1. Cascade and atomic refunds

- [x] 1.1 Add `IndividualTicket.clearPasses()`/`restorePasses(...)`; verify with `PersonalPassTest`
- [x] 1.2 Inject the `PassRepository` into `TicketManager` (shared instance from `Bootstrap`), persist passes with tickets, implement ordered cascade for `TICKET DELETE` and `GROUPTICKET DELETE` with rollback on storage failure; verify with `TicketManagerTest` (cascade, restart, not refundable, storage failure restores state)

## 2. Persistence checks

- [x] 2.1 Verify restart of passes (price, consumed) and attraction types, and `--fly` storing `passes.txt`/`attractions.txt` only in the temporary directory, with `AppTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` with the new association and verify `plantuml -checkonly` and `UmlModelTest`
- [x] 3.2 Verify end to end with `mvn package`: refund a ticket and a group ticket with passes and check `passes.txt`, `tickets.txt`, `grouptickets.txt` after a restart
