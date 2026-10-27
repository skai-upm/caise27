# Proposal

## Why

Milestone F3-C02 adds invoices for users and groups, generated on demand from the stored park state and constructed explicitly with the Builder pattern so each invoice contains only the sections that apply to its recipient and the available economic information.

## What Changes

- Add a common invoice-construction abstraction (`InvoiceBuilder`) with differentiated steps — recipient identification, sections, items with amounts, item details without amounts — and a concrete builder producing an immutable `Invoice` whose total is the sum of the items shown.
- Add an invoice director in the business layer that builds:
  - user invoices (`INVOICE CREATE;USER;<email>`, active users only): user identification and the individual tickets bought directly for the user — tickets belonging to a group-ticket composition are not counted;
  - group invoices (`INVOICE CREATE;GROUP;<groupIdentifier>`, existing groups): identifier, name and each group ticket bought by the group with its total, plus its composition as non-counted detail.
- Omit sections without information.
- Render invoices as readable text in the presentation layer; HELP lists `INVOICE CREATE`.
- Commit with the exact message `[F3-C02] Add invoice generation with builder`.

SpeedPass and Special Pass sections of user invoices depend on passes, introduced in F3-C06; the builder steps are generic, so those sections are added there without changing the builder.

## Capabilities

### New Capabilities
- `invoices`: User and group invoices built with the Builder pattern and the INVOICE CREATE command.

### Modified Capabilities
- `execution-control`: HELP also lists `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

## Impact

- New `business.invoice` package (`Invoice`, `InvoiceSection`, `InvoiceItem`, `InvoiceBuilder`, `StandardInvoiceBuilder`, `InvoiceController`), new `presentation.InvoiceCommands`. Invoices are not persisted.
- Ambiguities flagged: historical and active tickets are both invoiced (they are all purchases); a group invoice includes only group tickets still referencing the group (tickets of a deleted earlier group with the same identifier are not attributed to it); text layout is our choice (allowed by the source).
