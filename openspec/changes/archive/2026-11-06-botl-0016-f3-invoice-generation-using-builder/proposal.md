# Proposal

## Why

Milestone F3-C02 requires on-demand invoices for users and groups, built from the system's economic information with the Builder pattern. Each invoice contains only the sections that suit its recipient and the available data, and never counts group-ticket components twice. There is no invoicing today.

## What Changes

- Add `INVOICE CREATE;USER;<email>`. The user must be an active registered user. The invoice shows the user's identifying information (email, DNI) and the individual tickets bought directly for that user with their prices, plus a total. Tickets that compose a group ticket are not invoiced to the user.
- Add `INVOICE CREATE;GROUP;<groupIdentifier>`. The group must exist. The invoice shows the group identifier and name and each group ticket of the group with its total price. Each ticket's composition is listed as descriptive detail without adding amounts, and the invoice total is the sum of the group ticket totals.
- Build invoices with the Builder pattern: a common builder abstraction with shared steps, plus concrete builders for user and group invoices that add only their own steps. Empty sections are omitted, and the total is computed from the items shown. The product is not persisted.
- List both forms in HELP.

### In scope
- User and group invoices for the tickets that exist today, the builder design and the command.

### Out of scope
- SpeedPass and Special Pass sections. Passes do not exist yet. The passes milestone (F3) adds their builder steps to the user invoice, and the builder is designed so that this adds steps without changing existing ones.
- Persisting invoices.

## Capabilities

### New Capabilities
- `invoices`: on-demand user and group invoices built section by section from the system's tickets, without double counting.

### Modified Capabilities
- None.

## Impact

- New package `es.upm.park.business.invoice` (`Invoice`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`).
- `ParkController` gains `userInvoice`/`groupInvoice` (the director role). `CommandProcessor` gets the INVOICE CREATE handler and HELP lines.
- UML model and its consistency check gain the invoice package. New tests.
