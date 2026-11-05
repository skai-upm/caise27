## Why

Delivery 3 (milestone F3-C02, source sections 6, 6.1, 6.2 and 6.3) requires on-demand invoices for users and groups, computed from the persisted economic information of the park. The invoices must be built with the Builder pattern: each one is assembled step by step, with only the sections that apply to its recipient and that have content. Items already paid inside a group ticket must not be counted twice.

## What Changes

- New command `INVOICE CREATE;USER;<email>`. It builds an invoice for an existing user, with the user's identification, the individual tickets bought directly for that user, the price of each item and the total. Tickets that are components of a group ticket are excluded, because the group ticket already includes their price.
- New command `INVOICE CREATE;GROUP;<groupIdentifier>`. It builds an invoice for an existing group, with the group identifier and name, the group tickets bought for the group, the composition of each group ticket as descriptive detail, the price of each group ticket and the total. Component tickets and members' personal passes are not added to the group total.
- Builder pattern:
  - A common abstract builder, `InvoiceBuilder`, defines the construction steps: recipient, purchases, personal passes, then the result.
  - Two concrete builders, `UserInvoiceBuilder` and `GroupInvoiceBuilder`, implement those steps.
  - A director, `InvoiceManager`, finds the recipient and runs the steps in order.
  - The `Invoice` product only receives the sections that were actually built; empty sections are omitted, and there is no constructor with optional or `null` parameters.
- Invoices are generated on demand and are not persisted. Generating one never changes stored data.
- **HELP** lists the two new invoice commands, before `HELP` and `EXIT`.

## Capabilities

### New Capabilities
- `invoices`: user and group invoice generation built with the Builder pattern, with no double-counting of group-ticket components.

### Modified Capabilities
- `execution-control`: the HELP command list now includes the invoice commands.

## Impact

- `business.model`: new `Invoice`, `InvoiceSection` and `InvoiceItem` product classes.
- `business`: new `InvoiceBuilder` (abstract), `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceManager` (director).
- `presentation`: new `InvoiceCommands` (parsing and text formatting), plus dispatch and HELP changes in `CommandProcessor`.
- `uml/model.puml`, tests and README are updated. No persistence format changes.
- SpeedPasses and Special Passes do not exist yet; they arrive with milestone F3-C05 (proposal 27). The user builder's personal-passes step is defined now, but it produces no section until passes exist, consistent with "sections with no information must be omitted".
