# Proposal

## Why

The park needs on-demand invoices for users and groups summarising what they bought. Invoices differ by recipient and by the data available, so they must be assembled step by step with the Builder pattern instead of constructors with many optional parameters.

## What Changes

- New commands `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<identificadorGrupo>`, listed in `HELP`.
- A Builder abstraction (`InvoiceBuilder`) with a concrete builder and a director that assembles invoices in distinct steps (header, applicable sections, total).
- A user invoice lists the individual tickets bought directly for the user; tickets generated inside a group ticket are not counted again.
- A group invoice lists the group's group-ticket purchases and their total, without repeating their internal tickets.
- Each invoice shows the price of every included item and a total; sections without data are omitted.
- Invoices are generated on demand and never persisted.
- Personal passes do not exist yet; the passes section of the user invoice will be added by the change that introduces passes (F3-C06), using the same builder.

## Capabilities

### New Capabilities
- `invoices`: On-demand invoice generation for users and groups.

### Modified Capabilities
- `uml-model`: the model must show the Builder structure.

## Impact

- New package `es.upm.parque.business.invoice` (invoice product, builder interface and implementation, director) and `InvoiceController`; `CommandProcessor`, `App`, `uml/model.puml` updated.
- No persistence changes.
