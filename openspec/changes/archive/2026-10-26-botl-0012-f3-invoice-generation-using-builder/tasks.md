# Tasks

## 1. Builder

- [x] 1.1 Create `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` and `TextInvoiceBuilder` in `business.invoice`; verify with unit tests that steps assemble sections, empty sections are skipped, totals add up and `startInvoice` resets state

## 2. Director and command

- [x] 2.1 Create `InvoiceController` (user and group invoices; internal tickets excluded from user invoices; unknown user/group rejected); verify with unit tests for each spec scenario
- [x] 2.2 Add `INVOICE CREATE;USER|GROUP;<id>` to `CommandProcessor` and HELP and wire it in `App`; verify with command tests and a jar session

## 3. Model and verification

- [x] 3.1 Update `uml/model.puml` with the invoice classes; verify with `plantuml -checkonly`
- [x] 3.2 Run `mvn test` and the regression script; verify all tests pass and the previous output is unchanged except HELP
