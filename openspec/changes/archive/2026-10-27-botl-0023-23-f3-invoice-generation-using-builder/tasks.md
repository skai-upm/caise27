# Tasks

## 1. Builder and invoice model

- [x] 1.1 Create `Invoice`, `InvoiceSection`, `InvoiceLine`, abstract `InvoiceBuilder`, `UserInvoiceBuilder` and `GroupInvoiceBuilder`; verify with `InvoiceBuilderTest` totals, omitted empty sections, detail lines not counted, and builder hierarchy

## 2. Invoice generation

- [x] 2.1 Create `InvoiceService` (director) and `InvoiceOperationException`; verify with `InvoiceServiceTest` user invoice (direct tickets only, composed tickets excluded, empty user), group invoice (composition, total), unknown user/group
- [x] 2.2 Dispatch `INVOICE CREATE;USER|GROUP;...` in `CommandProcessor` with HELP lines, update `uml/model.puml`; verify with `CommandProcessorTest`, `UmlModelTest`, PlantUML syntax check and `mvn package`
