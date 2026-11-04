# Proposal

## Why

The park records individual tickets and group tickets with their prices, but there is no way to obtain an accounting view for a user or a group. Delivery 3 adds on-demand invoices, assembled step by step with the Builder pattern so that each recipient type only gets the sections that apply to it and no purchase is charged twice.

## What Changes

- New commands `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<identificadorGrupo>`.
- New immutable invoice model: `Invoice` (recipient, sections, total) made of `InvoiceSection`s (title, lines, subtotal) and `InvoiceLine`s (description, amount).
- Builder pattern: a common `InvoiceBuilder` abstraction with distinct construction steps (recipient, individual tickets section, group tickets section, total), two concrete builders (`UserInvoiceBuilder`, `GroupInvoiceBuilder`) and an `InvoiceDirector` that runs the steps in order. Variants are not handled with constructors taking many optional parameters.
- User invoice: individual tickets purchased directly for that user (`TICKET CREATE`), each with its price, and the total. Constituent tickets of group tickets are not charged.
- Group invoice: the group tickets purchased by that group, each with its total amount, and the overall total; the constituent individual tickets are not added again.
- Sections without data are omitted; the total is always shown. Invoices are not persisted.
- `HELP` lists the two commands; UML updated.

## Capabilities

### New Capabilities
- `invoices`: Invoice commands, contents per recipient type, section omission, totals, and the Builder-based construction.

### Modified Capabilities
- None.

## Impact

- **Code**: new `business.model.Invoice`, `InvoiceSection`, `InvoiceLine`; new `business.service.InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector`; `TicketService` gains read-only queries (`directTicketsOf(User)`, `groupTicketsOf(Group)`); `ParkController.createUserInvoice/createGroupInvoice`; `CommandProcessor` command, formatting and HELP; `uml/model.puml`.
- **Data**: none (no persistence).
- **Mandatory milestone message**: `[F3-C02] Add invoice generation with builder`.
- **Open questions / assumptions**:
  - Personal passes do not exist yet in the code (they are introduced by a later F3 change). No passes section is invented now; the builder abstraction is the extension point where that step will be added together with passes.
  - All of the recipient's purchases are included, past and future (the source sets no date filter).
  - An invoice for an existing user/group without purchases is still produced, with no sections and total 0.00; a nonexistent user/group or an unknown recipient type (`INVOICE CREATE;OTHER;x`) is rejected with `ERROR:`.
  - Invoices for a deleted user or group cannot be requested (the recipient must exist).
  - The textual layout of the invoice is ours (the source fixes only its content).
