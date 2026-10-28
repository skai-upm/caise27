# Proposal

## Why

Milestone F3-C02 adds on-demand invoices for users and groups, built with the Builder pattern so each invoice contains only the sections that apply to its recipient and the available economic information, without counting group-ticket items twice.

## What Changes

- Add `INVOICE CREATE;USER;<email>` (active user) and `INVOICE CREATE;GROUP;<groupIdentifier>` (existing group).
- Introduce an invoice product (header, sections of items, total) and a common builder abstraction with step-by-step construction; concrete builders for user and group invoices; a director (invoice service) that inspects the system's tickets and calls only the relevant steps.
- User invoice: user identification; individual tickets bought directly for the user (tickets in a group ticket composition are not charged again); each item's price; total. Pass sections (SpeedPass/Special Pass) are defined by F3-C06 and will be added as further builder steps.
- Group invoice: group identifier and name; group tickets bought by the group with their composition (informational, not added to the total) and total price; invoice total.
- Sections without information are omitted. Invoices are not persisted.
- `HELP` lists both forms.
- Commit with the mandatory message `[F3-C02] Add invoice generation with builder`.

## Capabilities

### New Capabilities
- `invoices`: User and group invoices, their contents, totals, omission of empty sections and the no-double-counting rule.

### Modified Capabilities
- `execution-control`: `HELP` also lists `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

## Impact

- New `business.invoice` package (product, builder abstraction, concrete builders), `business.service.InvoiceService` (director), presentation formatter and command.
- Ambiguities flagged:
  - Invoices include all tickets of the recipient, historical and future, since they describe purchases (fiscal records).
  - Group invoices include group tickets whose group reference is that group (historical tickets of a deleted group have no group and cannot be invoiced to a new group with the same identifier).
  - Text layout is the student's choice; see the design for the format.
