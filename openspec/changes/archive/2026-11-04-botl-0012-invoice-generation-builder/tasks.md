# Tasks

## 1. Invoice model and builder

- [x] 1.1 Create `Invoice`, `InvoiceSection`, `InvoiceLine` (immutable, totals by `BigDecimal`); verify `InvoiceTest` for subtotal/total and read-only collections
- [x] 1.2 Create `InvoiceBuilder` (abstract steps, empty sections omitted), `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify `InvoiceBuilderTest` for step order, omitted sections and totals for both builders

## 2. Data selection and controller

- [x] 2.1 Add `TicketService.directTicketsOf(User)` (excluding constituents) and `groupTicketsOf(Group)`, and `ParkController.createUserInvoice/createGroupInvoice` (recipient must exist); verify `InvoiceServiceTest` for the double-counting scenarios

## 3. Command

- [x] 3.1 Add `INVOICE CREATE;USER|GROUP;id` parsing, rendering and HELP lines to `CommandProcessor`; verify `InvoiceCommandsTest` (user with direct + constituent ticket, group invoice, recipient without purchases, unknown recipient/type, no invoice file written)

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` with the invoice classes, the builder hierarchy and relationships; verify `UmlModelTest`, `LayeringTest`, `plantuml -checkonly`, `mvn test` and `mvn package`
