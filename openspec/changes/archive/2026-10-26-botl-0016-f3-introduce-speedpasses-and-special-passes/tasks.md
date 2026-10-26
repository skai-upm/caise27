# Tasks

## 1. Model and persistence

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` and add pass ownership to `IndividualTicket`; verify `PassTest` (consume once, kept after consumption, ticket holds many SpeedPasses and one Special Pass)
- [x] 1.2 Extend `TicketRepository` with `data/passes.txt` and wire the path in `App`; verify `TicketRepositoryTest` round-trips passes attached to standalone and internal tickets with price and status, and skips unknown tickets

## 2. Pass rules

- [x] 2.1 Create the `PassController` singleton (buy 1/10 SpeedPasses, buy Special Pass, refunds, id generation, date rules) and `TicketController.findIndividualTicket/save/getIndividualTicketsOf`; verify `PassControllerTest` for every personal-passes scenario including restart with consumed status and fresh ids

## 3. Commands and invoices

- [x] 3.1 Introduce the `ParkControllers` parameter object for `CommandProcessor`; add SPEEDPASS CREATE/DELETE and SPECIALPASS CREATE/DELETE (registered with descriptions); verify `CommandProcessorTest` outputs and the existing suite
- [x] 3.2 Add the personal passes section to user invoices; verify `InvoiceControllerTest` (passes on standalone and internal tickets listed, group invoice unchanged)

## 4. Model and end-to-end

- [x] 4.1 Update `uml/model.puml` with the pass hierarchy, ticket composition, `PassController` and `ParkControllers`; verify PlantUML `-checkonly` and the UML cross-check script
- [x] 4.2 Run `mvn package`, the regression script (only new HELP lines may differ) and an end-to-end pass scenario with restart through the JAR; verify outputs
