# Tasks

## 1. Model and persistence

- [x] 1.1 Create `PersonalPass`, `SpeedPass`, `SpecialPass` and the pass composition in `IndividualTicket`; verify with `PersonalPassTest` and `IndividualTicketTest`
- [x] 1.2 Create `PassRepository` and `TextFilePassRepository` (format, attach to tickets, skip unknown tickets and malformed lines); verify with `TextFilePassRepositoryTest`

## 2. Operations

- [x] 2.1 Add `TicketManager.findAnyIndividual`, `allIndividualTickets`, `individualTicketsOf`; create the `PassManager` singleton (create speed 1/10, create special, refunds, consume, persistence); verify with `PassManagerTest` covering every personal-passes scenario including restart
- [x] 2.2 Introduce the `Controllers` holder, add the four pass commands to `CommandProcessor` and `CommandRegistry`, wire `PassManager` in `Bootstrap`/`Park`; verify with `CommandProcessorTest`
- [x] 2.3 Add the `Personal passes` section to user invoices; verify with `InvoiceManagerTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` (pass hierarchy, compositions, PassManager, repository, Controllers); verify `plantuml -checkonly` and `UmlModelTest`
- [x] 3.2 Verify end to end with `mvn package` and a command file buying, refunding and invoicing passes, then restarting
