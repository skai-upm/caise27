# Proposal

## Why

Milestone F3-C02 adds on-demand invoices for users and groups, built with the Builder pattern from the persisted economic information, including only the sections relevant to each recipient and never counting a ticket twice (individual tickets inside a group ticket are paid by the group ticket).

## What Changes

- `INVOICE CREATE;USER;<email>`: invoice for an active user with identification and the individual tickets bought directly for the user (each price and the total); tickets that belong to a group ticket are not included.
- `INVOICE CREATE;GROUP;<groupIdentifier>`: invoice for an existing group with its identifier and name, its group tickets (total price and composition) and the total; composed tickets are not added separately.
- Invoices are built step by step through a common builder abstraction with user and group builders; sections without information are omitted. Invoices are not persisted.
- SpeedPass and Special Pass sections are part of the user invoice design but have no data until passes exist (F3-C06), so they are omitted for now.
- HELP lists both INVOICE commands.
- Record the mandatory milestone commit `[F3-C02] Add invoice generation with builder` once verified.

## Capabilities

### New Capabilities
- `invoicing`: Generation of user and group invoices from the park's economic information.

### Modified Capabilities
- `command-processing`: HELP lists the INVOICE commands.

## Impact

- New package `business.invoice` (`Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`) and `business.service.InvoiceService`, `InvoiceOperationException`; `CommandProcessor` dispatch and HELP; UML model updated.
