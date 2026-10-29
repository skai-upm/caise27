# Tasks

## 1. Builder

- [x] 1.1 Create `business.invoice` (`Invoice`, `InvoiceSection`, `InvoiceLine`, abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector`); verify with unit tests that totals sum only priced lines, empty sections are omitted, and each builder only offers its own steps

## 2. Operations and commands

- [x] 2.1 Add `createUserInvoice` and `createGroupInvoice` to `ParkController`; verify with unit tests for direct tickets (total 85.50), components excluded, group invoice of the 6-member 192.00 example with composition, members' own tickets excluded, no-ticket group, unknown user/group
- [x] 2.2 Dispatch `INVOICE CREATE;USER;<email>` / `INVOICE CREATE;GROUP;<groupIdentifier>` (wrong target or argument count rejected) and add both to HELP; verify with processor tests and HELP output
- [x] 2.3 Add the new classes and relationships to `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` (including UML and architecture tests) and a JAR run of `INVOICE CREATE;GROUP;G01`
