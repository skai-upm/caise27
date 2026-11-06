# Tasks

## 1. Builder

- [x] 1.1 Add `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` and `TextInvoiceBuilder` (empty sections dropped, total = priced items) and verify with unit tests for step order, omitted empty sections, details excluded from the total, and reset between builds

## 2. Director and command

- [x] 2.1 Add `InvoiceService` (user and group invoices as directors) and verify with unit tests: direct tickets only for users (constituents excluded), user without purchases, group tickets with composition details and correct total, unknown user/group
- [x] 2.2 Expose the service from `ParkController`, dispatch `INVOICE CREATE;USER|GROUP;<id>` (unknown type rejected), list it in `HELP`, and verify with `CommandProcessor` tests

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with purchases and both invoice commands and verify the printed invoices
- [x] 3.2 Run `openspec validate botl-0023-invoice-generation-using-builder --strict` and verify the change is valid
