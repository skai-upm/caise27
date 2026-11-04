# Tasks

## 1. Model and storage

- [x] 1.1 Add `Pass`, `SpeedPass`, `SpecialPass` and `PassFileStorage` (`kind;id;ticketId;price;consumed`); add `passes.txt` to `DataFiles`; verify with `PassFileStorageTest`

## 2. Pass rules

- [x] 2.1 Add `TicketManager.findIndividualTicket` and singleton `PassManager` (speed 1/10 with derived ids, special once per ticket, 20%/100% prices, atomic checks, refund rules, consumed kept) wired in `ParkSession`; verify with `PassManagerTest`

## 3. Commands and invoices

- [x] 3.1 Add the four pass commands to `ParkController`/`CommandProcessor`/`CommandCatalog` with the exact replies; verify with `CommandProcessorTest` (source scenarios, invalid quantities, conflicts, refunds, constituent tickets)
- [x] 3.2 Add the `buildPasses` invoice step (user invoices include passes on owned tickets incl. constituents; group invoices exclude them); verify with `InvoiceBuilderTest` and a `CommandProcessorTest` invoice case

## 4. Model and regression

- [x] 4.1 Update `uml/model.puml`; verify `UmlModelTest`, `LayeringTest`, PlantUML `-syntax`
- [x] 4.2 Run `mvn clean package` and the E1/E2 regression script (identical outputs apart from the HELP lines) plus a manual pass session across two executions
