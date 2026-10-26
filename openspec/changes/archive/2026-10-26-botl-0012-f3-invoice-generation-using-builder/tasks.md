# Tasks

## 1. Invoice product and builders

- [x] 1.1 Create `Invoice`, `InvoiceSection`, `InvoiceLine` (immutable, subtotal/total); verify with `InvoiceTest` (totals, empty invoice 0.00)
- [x] 1.2 Create abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify with `InvoiceBuilderTest` (user invoice excludes internal tickets, group invoice excludes internals and members' tickets, empty sections omitted, step order via director)

## 2. Command and model

- [x] 2.1 Add `ParkController.createUserInvoice/createGroupInvoice`, route and format `INVOICE CREATE;USER|GROUP;...` in `CommandProcessor`, add HELP lines; verify with `CommandProcessorTest` (user, group, unknown recipient/type, no files changed) and `mvn test`
- [x] 2.2 Update `uml/model.puml` with the invoice and builder classes; verify `plantuml -checkonly` and the field cross-check pass
