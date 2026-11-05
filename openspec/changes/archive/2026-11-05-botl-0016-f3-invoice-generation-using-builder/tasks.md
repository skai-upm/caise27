# Tasks

## 1. Builder model

- [x] 1.1 Implement `Invoice`, `InvoiceBuilder`, `UserInvoiceBuilder` and `GroupInvoiceBuilder` in `business.invoice`; verify unit tests for empty-section omission, counted versus detail lines, and totals

## 2. Director

- [x] 2.1 Add `ParkController.createUserInvoice` and `createGroupInvoice` (exclusion of component tickets and members' purchases, unknown recipients); verify unit tests for each scenario

## 3. Command

- [x] 3.1 Implement `InvoiceCommands` (`INVOICE CREATE;USER|GROUP;...`), register it in `App.registerCommands`, list both forms in `HELP`, and update the UML model; verify command tests and `UmlModelTest`

## 4. Verification

- [x] 4.1 Run `mvn -q package` and verify all tests pass
