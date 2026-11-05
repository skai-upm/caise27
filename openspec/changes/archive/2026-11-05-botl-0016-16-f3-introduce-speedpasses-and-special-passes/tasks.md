# Tasks

## 1. Model and persistence

- [x] 1.1 Implement `Pass`, `SpeedPass`, `SpecialPass` (pricing, consume) and `PassRepository`/`PassFileStore` (line format, history lines); add `TicketManager.findIndividualTicket`; verify with `PassTest` and `PassFileStoreTest`

## 2. Pass management

- [x] 2.1 Implement singleton `PassManager` (buy SpeedPasses 1/10 all-or-nothing, buy Special Pass once per ticket, refund rules, restart) wired by `ApplicationContext` and `ParkController`; verify with `PassManagerTest` covering every spec scenario including consumed passes and constituent tickets

## 3. Commands, invoice, docs

- [x] 3.1 Parse `SPEEDPASS CREATE|DELETE` and `SPECIALPASS CREATE|DELETE`, add catalog entries, add the invoice pass step; verify with `CommandProcessorTest` (purchase outputs, refunds, invalid commands, user invoice with passes, HELP lists commands) and `mvn test`
- [x] 3.2 Run the E1/E2 script against the previous JAR (only HELP lines may differ), the layer check, and update `uml/model.puml` with PlantUML `-checkonly` and the cross-check
