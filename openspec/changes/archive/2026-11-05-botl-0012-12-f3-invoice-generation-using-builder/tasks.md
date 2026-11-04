# Tasks

## 1. Builder

- [x] 1.1 Implement `Invoice` (sections, lines, total, `format`) and the abstract `InvoiceBuilder` with `reset`, no-op step defaults, empty-section omission and `getResult`; verify with `InvoiceDirectorTest` (empty sections omitted, total equals sum, product immutable, builder reusable)
- [x] 1.2 Implement `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceDirector`; verify with `InvoiceDirectorTest` (same step sequence, user builder ignores group tickets, group builder ignores individual tickets, discount and count shown)

## 2. Commands

- [x] 2.1 Add `ParkController.createUserInvoice/createGroupInvoice` (data selection without constituents) and `INVOICE CREATE;USER|GROUP;...` parsing plus HELP lines; verify with `CommandProcessorTest` (direct ticket charged once with group membership, group invoice with 3-member discount, empty recipient total 0.00, unknown recipient, malformed commands, no data files changed)

## 3. Model and regression

- [x] 3.1 Update `uml/model.puml` with the invoice classes; verify PlantUML `-checkonly`, the code cross-check and the layer check pass, and `mvn test` passes
