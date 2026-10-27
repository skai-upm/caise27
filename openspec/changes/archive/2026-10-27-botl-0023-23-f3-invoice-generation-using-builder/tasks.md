# Tasks

## 1. Builder

- [x] 1.1 Create `business.invoice` product classes, the `InvoiceBuilder` interface and `StandardInvoiceBuilder` (total from items, empty sections dropped); verify with `InvoiceBuilderTest`

## 2. Invoices

- [x] 2.1 Create `InvoiceController` building user and group invoices (direct tickets only for users; group tickets with composition details for groups); verify the invoices scenarios in `InvoiceControllerTest`
- [x] 2.2 Add `INVOICE CREATE;USER|GROUP;…` in presentation with text rendering and list it in HELP; verify command output and error cases in `InvoiceCommandsTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C02] Add invoice generation with builder`; verify with `git log -1 --format=%s`
