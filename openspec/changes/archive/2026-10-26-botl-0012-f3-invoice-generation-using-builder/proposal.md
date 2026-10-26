# Proposal

## Why

The system records every ticket purchase with its price, but users and groups cannot get a statement of what they bought. Delivery 3 asks for on-demand invoices built step by step with the Builder pattern, so different invoice variants are assembled from the applicable sections instead of through constructors with many optional parameters.

## What Changes

- New commands:
  - `INVOICE CREATE;USER;<correo>` — invoice for an existing user: the individual tickets bought directly for that user (tickets that are internal to a group ticket are not counted again), and the personal passes attached to the user's tickets once passes exist in the system.
  - `INVOICE CREATE;GROUP;<identificadorGrupo>` — invoice for an existing group: its group ticket purchases with their total amounts, without listing again the internal individual tickets or the members' personal passes.
- Invoices are assembled progressively through a Builder abstraction (recipient, then each applicable section with its items, then the total), including only the sections that apply to the recipient type and have data.
- Each invoice shows the price of every included item and the total. Invoices are generated on demand and are not persisted.
- HELP lists the new command syntax.

## Capabilities

### New Capabilities
- `invoicing`: On-demand invoices for users and groups — which purchases each recipient type includes, step-by-step assembly with a Builder, sections shown only when applicable, item prices and total, and no persistence.

### Modified Capabilities
<!-- None -->

## Impact

- New `es.upm.parque.business.invoice` package (`Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder` interface, `StandardInvoiceBuilder`); new `InvoiceController` acting as director; `CommandProcessor` gains `INVOICE CREATE` and invoice formatting; `App`/`TestPark` wiring.
- `uml/model.puml` updated. Tests for both recipient types, double-counting rules and section selection. No new dependencies.
