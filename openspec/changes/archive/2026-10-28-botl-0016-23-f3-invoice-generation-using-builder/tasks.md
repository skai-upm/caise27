# Tasks

## 1. Invoice model and builders

- [x] 1.1 Add `Invoice`, `InvoiceSection`, `InvoiceItem` and the abstract `InvoiceBuilder` (empty sections omitted, total from shown items); verify with unit tests
- [x] 1.2 Add `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify with unit tests (direct tickets only, composition details, group totals, empty recipients)

## 2. Controller and command

- [x] 2.1 Add `createUserInvoice`/`createGroupInvoice` to `ParkController`; verify with controller tests (active user required, existing group required, composed tickets excluded)
- [x] 2.2 Add `InvoiceCreateCommand` and `InvoiceFormatter`, register the command and add both forms to HELP; verify with command tests (rendered totals, unknown type, unknown recipient) and the HELP test

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with a command file requesting a group invoice and an invalid invoice; verify output
- [x] 3.2 Commit with the exact message `[F3-C02] Add invoice generation with builder`; verify with `git log -1 --format=%s`
