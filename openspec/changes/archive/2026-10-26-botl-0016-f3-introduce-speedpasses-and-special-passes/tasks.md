# Tasks

## 1. Pass model

- [x] 1.1 Create `PassStatus`, abstract `Pass`, `SpeedPass`, `SpecialPass` and the pass composition in `IndividualTicket` (pricing, numbering, lifetime Special Pass rule, consume/refund transitions); verify with `PassTest`

## 2. Rules and persistence

- [x] 2.1 Add pass purchase/refund operations to `TicketManager` (individual tickets incl. internal, quantity 1/10, date rules) and persist them in `passes.txt` via `TicketStorage`; verify with `TicketManagerTest`/`TicketStorageTest` (restart restores statuses, ticket deletion drops passes)

## 3. Invoices, commands and model

- [x] 3.1 Add the personal-passes invoice step (`InvoiceBuilder`, `UserInvoiceBuilder`, `InvoiceDirector`); verify with `InvoiceBuilderTest` (refunded excluded, internal-ticket passes included, group invoice unchanged)
- [x] 3.2 Add `SPEEDPASS CREATE|DELETE` and `SPECIALPASS CREATE|DELETE` to `ParkController`, `CommandProcessor` and `CommandCatalog`, wire `passes.txt` in `App`; verify with `CommandProcessorTest` scenarios and `mvn test`
- [x] 3.3 Update `uml/model.puml` with the pass hierarchy and compositions; verify `plantuml -checkonly` and the field cross-check pass
