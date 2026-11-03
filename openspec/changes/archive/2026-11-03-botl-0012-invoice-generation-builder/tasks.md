# Tasks

## 1. Invoice product and builder

- [x] 1.1 Create `InvoiceLine`, `InvoiceSection`, `Invoice` (immutable, totals) and verify `InvoiceTest` totals and empty invoice total 0.00
- [x] 1.2 Create `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify `InvoiceBuilderTest` that each builder adds only its applicable non-empty section and that the director runs all steps

## 2. Controller and command

- [x] 2.1 Add `ParkController.userInvoice(email)` / `groupInvoice(id)` (direct tickets only / group tickets only, captured prices, system date) and `INVOICE CREATE;USER|GROUP;...` with rendering and errors to `CommandProcessor`, plus HELP; verify `InvoiceCommandsTest` for the user/group scenarios, no double counting, empty sections, unknown recipient/type, and that no data file changes

## 3. UML and verification

- [x] 3.1 Update `uml/model.puml` with the invoice classes (same notation); verify `plantuml -checkonly`, then `mvn clean test` and `mvn package` pass
