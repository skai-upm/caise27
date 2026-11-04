# Tasks

## 1. Invoice product and builders

- [x] 1.1 Create `business.invoice` with `Invoice`, `InvoiceSection`, `InvoiceLine` (immutable), abstract `InvoiceBuilder` (steps, empty-section omission, total), `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify with `InvoiceBuilderTest` (direct tickets only, group tickets only, captured prices, empty sections omitted, total 0.00, same director steps for both builders)

## 2. Controller and command

- [x] 2.1 Add `createUserInvoice`/`createGroupInvoice` to `ParkController` and `INVOICE CREATE;USER|GROUP;<id>` parsing/formatting plus `HELP_LINES` to `CommandProcessor`; verify with `CommandProcessorTest` cases (no double counting with a group ticket, price change, group invoice excludes members' own tickets, no purchases, unknown user/group, malformed commands, no data file changed)

## 3. Model and integration

- [x] 3.1 Update `uml/model.puml` with the invoice classes, inheritance and relationships; verify PlantUML `-syntax`, `UmlModelTest` and the label/field check
- [x] 3.2 Run `mvn clean test` and the packaged JAR with an invoice scenario
