# Proposal

## Why

The system records individual and group ticket purchases, but there is no way to see what a user or group has been charged. On-demand invoices are needed. Their content varies by recipient type and available data, so the source requires them to be assembled step by step with the Builder pattern instead of with constructors that take many optional parameters.

## What Changes

- New commands `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<identificadorGrupo>`.
- New immutable invoice model: `Invoice` (recipient, sections, total), `InvoiceSection` (title, lines, subtotal), `InvoiceLine` (description, amount).
- Builder pattern:
  - abstract `InvoiceBuilder` with distinct steps (`buildHeader`, `buildIndividualTickets`, `buildGroupTickets`) and `getResult()`;
  - concrete `UserInvoiceBuilder` and `GroupInvoiceBuilder`;
  - `InvoiceDirector` runs the steps in order;
  - sections without data are omitted.
- A user invoice lists the user's directly purchased individual tickets with their captured prices. Constituent tickets of group tickets are not charged.
- A group invoice lists the group's group-ticket purchases with their stored totals; constituent tickets are not added again.
- Invoices are generated on demand and not persisted. Unknown users or groups are rejected.
- `HELP` lists the new commands.
- **Deferred:** personal passes do not exist yet (introduced in a later F3 change). The pass section and "passes attached to constituent tickets" are left for that change, which will add a builder step. No pass code is added now.

## Capabilities

### New Capabilities
- `invoices`: On-demand user and group invoices, their content rules and Builder-based construction.

### Modified Capabilities
- `application-control`: "HELP command" lists the two `INVOICE CREATE` forms.

## Impact

- New: `Invoice`, `InvoiceSection`, `InvoiceLine` (business.model); `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector` (business.service); tests.
- Changed: `ParkController` (invoice operations), `CommandProcessor` (command and formatting), `uml/model.puml`.
- No file format change.
