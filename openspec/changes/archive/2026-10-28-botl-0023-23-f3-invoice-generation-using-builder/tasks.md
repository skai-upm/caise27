# Tasks

## 1. Builder

- [x] 1.1 Create `business.invoice` (`Invoice`, `InvoiceSection`, `InvoiceItem`, abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`); verify unit tests for totals, detail items not counted, and empty-section omission

## 2. Invoices

- [x] 2.1 Create `InvoiceService` (director) for user and group invoices following the inclusion rules; verify unit tests: direct tickets charged, composition tickets not charged to the user, group ticket totals with composition, unknown user/group, group without tickets
- [x] 2.2 Add `InvoiceFormatter` and the `INVOICE CREATE;USER|GROUP;...` command with `HELP` entries; verify processor tests for both forms and an unknown type
- [x] 2.3 Run `mvn package` and a JAR session with user and group invoices; verify output and totals

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F3-C02] Add invoice generation with builder`; verify with `git log -1 --format=%s`
