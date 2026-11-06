# Proposal

## Why

Milestone F3-C02 (Delivery 3, step 2) adds invoices for users and groups, built with the Builder pattern from the economic information in the system, including only the sections that apply to the recipient and the data available, and never counting a group ticket's constituent tickets twice.

## What Changes

- `INVOICE CREATE;USER;<email>` for an active user: user identification, the individual tickets purchased directly for that user (constituents of group tickets excluded), each item's price and the total. Sections for SpeedPasses and the Special Pass are part of the construction steps and appear only when such passes exist (they are introduced in F3-C06); empty sections are omitted.
- `INVOICE CREATE;GROUP;<group>` for an existing group: group identifier and name, its group tickets with total price, their composition as non-priced detail, and the total. Members' passes are not added.
- A common invoice-building abstraction with explicit construction steps, a concrete builder producing an immutable invoice, and a director (the invoice service) that applies only the relevant steps for each invoice type. Invoices are generated on demand and not persisted.
- `HELP` lists both invoice commands.

### In scope
- Source sections 6, 6.1, 6.2, 6.3.

### Out of scope
- SpeedPass/Special Pass purchase (F3-C06); invoice persistence.

## Capabilities

### New Capabilities
- `invoices`: user and group invoice content, conditional sections, totals and builder-based construction.

### Modified Capabilities
- `execution-control`: `HELP` lists `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<group>`.

## Impact

- New package `es.upm.park.business.invoice` (`Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `TextInvoiceBuilder`) and `business.service.InvoiceService`; `ParkController` exposes it; `CommandProcessor` dispatch.
