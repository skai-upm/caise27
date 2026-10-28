# Proposal

## Why

Milestone F3-C02 adds on-demand invoices for users (`INVOICE CREATE;USER;<email>`) and groups (`INVOICE CREATE;GROUP;<groupIdentifier>`) built with an explicit Builder pattern: each invoice is assembled step by step from sections relevant to its recipient and the available economic information, empty sections are omitted, and items already paid within a group ticket are never counted twice.

## What Changes

- Add an invoice model (invoice, sections, item lines with amount and optional detail lines, total) created only through builders.
- Add a common builder abstraction with two concrete builders (user, group) exposing differentiated construction steps, and a director that runs the steps for each invoice type.
- User invoice: user identification, individual tickets bought directly for the user (not those inside group tickets), item prices and total.
- Group invoice: group identifier and name, its group tickets with their composition, each group ticket's total price and the invoice total (composed individual tickets not added separately).
- Add `INVOICE CREATE;USER;<email>` (active user required) and `INVOICE CREATE;GROUP;<groupIdentifier>` (existing group required); presentation renders the invoice as readable text.
- Extend `HELP`.
- Commit with the exact message `[F3-C02] Add invoice generation with builder`.

## Capabilities

### New Capabilities
- `invoices`: User and group invoice generation and content rules.

### Modified Capabilities
- `execution-control`: HELP lists the two INVOICE CREATE forms.

## Impact

- New `business.invoice` package, controller invoice operations, presentation command and formatter, tests.
- **Flagged ambiguities / ordering conflicts:**
  - SpeedPasses and Special Passes (F3-C06) do not exist yet. The user builder is designed with a dedicated step for pass sections, which F3-C06 will implement; until then user invoices contain no pass section (sections without information are omitted anyway).
  - Whether historical (past-dated) tickets appear on invoices is not specified; invoices list all purchases of the recipient regardless of date, since they are economic records.
  - The textual layout is free; a header, one block per section, item lines with amounts and a `TOTAL` line are used.
