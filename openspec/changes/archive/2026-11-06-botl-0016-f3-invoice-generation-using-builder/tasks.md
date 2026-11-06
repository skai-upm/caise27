# Tasks

## 1. Builder and product

- [x] 1.1 Add `Invoice` (sections, items, details, total, render) and the abstract `InvoiceBuilder` with shared steps and empty-section omission, and verify with `InvoiceBuilderTest` that empty sections are dropped and that detail lines do not change the total
- [x] 1.2 Add `UserInvoiceBuilder` and `GroupInvoiceBuilder` with their specific steps, and verify with tests that composed tickets are excluded from user invoices and that group invoices list the composition without adding it to the total

## 2. Director and command

- [x] 2.1 Add `ParkController.userInvoice`/`groupInvoice` (recipient validation, ticket selection by traversal), and verify with tests the 85.50 user example, the 171.00 group example, the user without purchases, and unknown recipients
- [x] 2.2 Add the `INVOICE CREATE` handler (USER/GROUP, argument errors) and HELP lines, and verify with `InvoiceCommandsTest`

## 3. Documentation

- [x] 3.1 Add the invoice package to `uml/model.puml` and to the UML consistency check, document INVOICE CREATE in `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
