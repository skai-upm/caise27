# Tasks

## 1. Builder structure

- [x] 1.1 Create `InvoiceLine`, `InvoiceSection` and `Invoice` (with total and text format); verify a unit test for totals and formatting
- [x] 1.2 Create the `InvoiceBuilder` interface and `DefaultInvoiceBuilder`; verify a unit test that header + sections + result produce the expected invoice and that `reset` clears previous state
- [x] 1.3 Create `InvoiceDirector` constructing user and group invoices with only non-empty sections; verify unit tests including an invoice without purchases (total 0.00)

## 2. Controller and commands

- [x] 2.1 Add `TicketController.directTicketsOf` and `groupTicketsOf`, and `InvoiceController`; verify tests that internal tickets of group tickets are excluded from user invoices and that group invoices list only group tickets
- [x] 2.2 Add `INVOICE CREATE;USER;...` and `INVOICE CREATE;GROUP;...` to `CommandProcessor` and HELP, wire in `App`; verify command tests (unknown user/group, wrong type, no data file written), then run `mvn test`

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with the invoice package and `InvoiceController`; verify `plantuml -checkonly` and the model/code cross-check pass
