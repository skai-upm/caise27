# Proposal

## Why

Milestone F3-C02 adds invoices for users and groups, generated on demand from the system's economic information and constructed with the Builder pattern: each invoice is assembled step by step with only the sections relevant to its recipient, and group ticket components are never counted twice.

## What Changes

- `INVOICE CREATE;USER;<email>`: invoice for an active user with the user's identifying data, the individual tickets purchased directly for them (not composition tickets of group tickets) with prices, and the total.
- `INVOICE CREATE;GROUP;<groupIdentifier>`: invoice for an existing group with its identifier and name, its group tickets with their composition (informational) and total price each, and the total.
- Empty sections are omitted. Invoices are not persisted.
- Builder design: a common invoice-construction abstraction with differentiated steps and specific builders for user and group invoices; the invoice service directs the construction.
- Out of scope: SpeedPass and Special Pass lines (they do not exist yet; F3-C06 adds them through a new builder step).

## Capabilities

### New Capabilities
- `invoices`: User and group invoice generation and their contents.

### Modified Capabilities
<!-- None -->

## Impact

- New `business.invoice` package (`Invoice`, `InvoiceLine`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`), `business.service.InvoiceService`; `INVOICE CREATE` command and HELP lines.
