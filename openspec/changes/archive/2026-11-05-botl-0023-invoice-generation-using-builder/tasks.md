# Tasks

## 1. Builder

- [x] 1.1 Create `business.invoice` (`InvoiceLine`, `Invoice`, abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`) with omitted empty sections and totals of counted lines; verify unit tests for builders, totals and section omission

## 2. Service and command

- [x] 2.1 Create `InvoiceService` (director) selecting direct tickets and group tickets, and register `INVOICE CREATE;USER|GROUP;<id>` with HELP lines; verify command tests for user/group invoices, no double counting, unknown user/group/type

## 3. Integration

- [x] 3.1 Run the JAR with ticket purchases and both invoice commands; run `mvn clean package` and verify all tests pass
