# Tasks

## 1. Invoice model and builder

- [x] 1.1 Add `InvoiceLine`, `InvoiceSection`, `Invoice` records (subtotal/total); verify with `InvoiceTest`
- [x] 1.2 Add abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify with `InvoiceBuilderTest` (no constituents on user invoice, group totals, empty sections omitted, zero total)

## 2. Commands

- [x] 2.1 Add `ParkController.createUserInvoice/createGroupInvoice` and the `INVOICE CREATE;USER|GROUP;<id>` command with formatting and HELP entries; verify with `CommandProcessorTest` (source scenarios, unknown recipient/type, wrong count, no data file change)

## 3. Model and regression

- [x] 3.1 Update `uml/model.puml` with the invoice classes; verify `UmlModelTest`, `LayeringTest` and the PlantUML `-syntax` check
- [x] 3.2 Run `mvn clean package` with all tests passing and a manual JAR run of both invoice forms
