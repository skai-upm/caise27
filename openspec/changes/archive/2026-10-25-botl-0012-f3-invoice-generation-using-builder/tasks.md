# Tasks

## 1. Builder and product

- [x] 1.1 Create `InvoiceLine`, `InvoiceSection`, `Invoice` (with `toText`) and the `InvoiceBuilder` interface with `StandardInvoiceBuilder` (steps, empty-section removal, total at build, line before section rejected); verify with `StandardInvoiceBuilderTest` and `InvoiceTest`

## 2. Director and command

- [x] 2.1 Add `TicketManager.ticketsHeldBy(User)` and `groupTicketsOf(Group)`, and create `InvoiceManager` (user and group invoices, unknown recipients, injected builder supplier); verify with `InvoiceManagerTest` covering every invoicing scenario
- [x] 2.2 Add `INVOICE CREATE` to `CommandProcessor` (type check, parameter count, HELP), wire `InvoiceManager` in `App` and `Park`; verify with `CommandProcessorTest`

## 3. Design model and integration

- [x] 3.1 Update `uml/model.puml` (interface, realisation, invoice classes and associations) and extend `UmlModelTest` for interfaces/realisations; verify `plantuml -checkonly` and `mvn test`
- [x] 3.2 Verify end to end with `mvn package` and a command file producing a user invoice (direct + group tickets) and a group invoice
