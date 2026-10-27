# Tasks

## 1. Pass model and storage

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` and `PassRepository`, add passes to `ParkRepositories`/`ParkController`; verify with `PassTest` prices (40.00 → 8.00 / 40.00; 32.00 → 6.40 / 32.00) and initial consumed state

## 2. Pass operations

- [x] 2.1 Create `PassService` and `PassOperationException` (SpeedPass 1/10 purchase, Special Pass purchase, refunds) and verify with `PassServiceTest` all purchase and refund scenarios of the spec
- [x] 2.2 Remove passes when tickets/group tickets are deleted (atomic) and add pass sections to user invoices; verify with `PassServiceTest`/`InvoiceServiceTest`

## 3. Commands

- [x] 3.1 Dispatch the four pass commands in `CommandProcessor`, add them to `CommandCatalog`, update `uml/model.puml`; verify with `CommandProcessorTest`, `HelpGeneratorTest`, `UmlModelTest`, PlantUML check and `mvn package`
