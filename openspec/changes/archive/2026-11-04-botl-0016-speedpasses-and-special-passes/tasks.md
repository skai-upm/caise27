# Tasks

## 1. Model and pricing

- [x] 1.1 Add `Pass`, `SpeedPass`, `SpecialPass` (prices from the ticket's stored price, consumed flag) and `GroupTicket.memberPrice` used for generated and reloaded constituent tickets; verify with `PassTest` and updated `GroupTicketTest` (36.00 per member for 3 at 40.00; totals unchanged)

## 2. Service and persistence

- [x] 2.1 Add `TicketManager.findIndividualTicket`, `PassFileRepository` and `PassManager` (pack ids, all-or-nothing checks, quantity 1/10, ticket on or after today, one Special Pass recorded per ticket, refunds only unconsumed with ticket after today, save with rollback, restart linking); verify with `PassManagerTest` and `PassFileRepositoryTest`

## 3. Controller, commands, invoices

- [x] 3.1 Wire `PassManager` into `App`/`ParkController`; add the four pass commands to `CommandProcessor` and `CommandCatalog`; guard ticket/group-ticket deletion when passes exist; ignore `passes.txt`; verify with `CommandProcessorTest` cases for every `passes` scenario
- [x] 3.2 Add the `buildPassSection` step (director, abstract builder no-op, user builder with personal passes); verify with `InvoiceBuilderTest`/`CommandProcessorTest` (passes on group-member tickets in the user invoice, never in the group invoice)

## 4. Model and integration

- [x] 4.1 Update `uml/model.puml`; verify PlantUML `-syntax`, `UmlModelTest` and the label/field check
- [x] 4.2 Run `mvn clean package` and the JAR with a purchase/refund scenario across a restart
