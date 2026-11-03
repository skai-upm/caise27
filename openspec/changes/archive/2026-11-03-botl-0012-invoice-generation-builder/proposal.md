# Proposal

## Why

Users and groups need on-demand invoices of what they have purchased. Invoices differ by recipient and must contain only the applicable sections, without charging the individual tickets inside a group ticket twice. The Builder pattern is required to assemble them step by step.

## What Changes

- New commands `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<identificadorGrupo>`.
- A Builder abstraction (`InvoiceBuilder`) with distinct construction steps (header, individual tickets, group tickets, result), concrete `UserInvoiceBuilder` and `GroupInvoiceBuilder`, and an `InvoiceDirector` that runs the steps; the resulting `Invoice` holds the recipient, issue date (system date), sections with priced lines and the total.
- User invoice: the user's directly purchased individual tickets; constituent tickets of group tickets are never charged to the user. Group invoice: the group's group tickets with their discounted totals, without constituent tickets.
- Sections without data are omitted; invoices are generated on demand and not persisted.
- Personal passes are not implemented yet (F3-C06); the builder is designed so a pass section can be added then (see design).
- HELP and `uml/model.puml` updated.

## Capabilities

### New Capabilities
- `invoices`: on-demand invoice generation for users and groups and its contents.

### Modified Capabilities
- `application-control`: HELP lists the two INVOICE commands.
- `domain-model-diagram`: the diagram also shows the invoice builder hierarchy.

## Impact

- New `business.invoice` package (`InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector`) and model classes `Invoice`, `InvoiceSection`, `InvoiceLine`; `ParkController` gains invoice operations; `CommandProcessor` renders invoices.
- No new files or formats.
