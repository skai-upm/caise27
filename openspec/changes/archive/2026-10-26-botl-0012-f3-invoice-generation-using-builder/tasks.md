# Tasks

## 1. Builder

- [x] 1.1 Create `Invoice`, `InvoiceSection`, `InvoiceLine`, the `InvoiceBuilder` interface and `StandardInvoiceBuilder` (empty sections dropped, total summed, reset after build); verify `StandardInvoiceBuilderTest` (steps, total, empty section omitted, reuse after build)

## 2. Director and queries

- [x] 2.1 Add `getStandaloneTicketsOf(User)` and `getGroupTicketsOf(Group)` to `TicketController`; create `InvoiceController` (`forUser`, `forGroup`) using a `Supplier<InvoiceBuilder>`; verify `InvoiceControllerTest` for direct tickets, internal tickets not counted, group totals, no sections when empty, unknown user/group

## 3. Command

- [x] 3.1 Add `INVOICE CREATE;Tipo;Identificador` to `CommandProcessor` (listed by HELP, USER/GROUP validation, formatting) and wire `App`/`TestPark`; verify `CommandProcessorTest` output for a user and a group invoice and that no data file changes

## 4. Model and end-to-end

- [x] 4.1 Update `uml/model.puml` with the invoice package and `InvoiceController`; verify PlantUML `-checkonly` and the UML cross-check script
- [x] 4.2 Run `mvn package` and an end-to-end JAR scenario with user and group invoices; verify outputs
