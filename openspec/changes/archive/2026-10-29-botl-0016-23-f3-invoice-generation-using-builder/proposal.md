# Proposal

## Why

Milestone F3-C02 adds on-demand invoices for users and groups, built with an explicit Builder pattern from the economic information in the system, including only the sections that apply to the recipient and to the available information, and never counting an individual ticket of a group ticket twice.

## What Changes

- `INVOICE CREATE;USER;<email>`: invoice for an existing (active) user — identifying data, the individual tickets purchased directly for that user with their prices, and the total. Individual tickets that are part of a group ticket composition are not listed as individual purchases.
- `INVOICE CREATE;GROUP;<groupIdentifier>`: invoice for an existing group — identifier and name, the group tickets bought by the group with their composition and total price, and the total; composed individual tickets are not added separately.
- A common invoice-construction abstraction (`InvoiceBuilder`) with concrete builders for user and group invoices and a director that applies only the relevant construction steps; sections without information are omitted. Invoices are not persisted.
- HELP lists both INVOICE commands.

## Capabilities

### New Capabilities
- `invoices`: user and group invoice content, conditional sections and totals.

### Modified Capabilities
- `command-interface`: HELP lists the INVOICE commands.

## Impact

- New package `business.invoice` (invoice product, builder abstraction, two builders, director); dispatch in `CommandProcessor`; UML updated; tests.

## Ambiguities and unmet prerequisites (flagged)

- **SpeedPasses and Special Passes** are introduced by a later Delivery 3 milestone (applied later in this batch). The user invoice builder is the extension point where that milestone adds a passes step; until then the passes section cannot appear, and group invoices never add members' passes to their total.
- **Active user:** users can still only be registered programmatically (USER CREATE comes later in this batch), so through commands INVOICE CREATE;USER reports the user as not found.
- Invoices include all purchases (active and historical), since historical tickets are kept as fiscal records. Tickets are listed in purchase order.
- Text layout is free per the source; a titled block with one line per item and amount, and a `TOTAL` line, is used.
