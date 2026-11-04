# Proposal

## Why

The system records individual and group ticket purchases but offers no accounting view. Users and groups need on-demand invoices that show what was charged to them, assembled step by step with the Builder pattern so each recipient type includes only its applicable sections and nothing is charged twice.

## What Changes

- New commands `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<identificadorGrupo>`.
- Invoices are built with a Builder abstraction (`InvoiceBuilder`) in distinct steps (header, directly purchased individual tickets, group-ticket purchases, total) by concrete user and group builders driven by a director; no constructors with many optional parameters.
- A user invoice lists the individual tickets bought directly for that user; individual tickets that belong to a group ticket are not charged again. A group invoice lists the group's group-ticket purchases and their amounts, without adding their constituent tickets.
- Each included item shows its price, and the invoice shows a total; sections without data are omitted; unknown users or groups are rejected.
- Invoices are generated on demand and not persisted.
- `HELP` lists the new commands; `uml/model.puml` reflects the new classes.

**Dependency on a later change:** the source also mentions personal passes associated with tickets. Passes do not exist yet (they are introduced by the later "SpeedPasses and Special Passes" change), so no pass section is built now; the builder is designed so that a pass step can be added then without changing existing steps.

The source suggests change ID `generate-invoices-with-builder`; this batch uses the BOTL ID `botl-0012-12-f3-invoice-generation-using-builder`. Mandatory milestone message: `[F3-C02] Add invoice generation with builder`.

## Capabilities

### New Capabilities
- `invoice-generation`: On-demand user and group invoices built with the Builder pattern.

### Modified Capabilities
- `application-control`: `HELP` lists `INVOICE CREATE;USER;Correo` and `INVOICE CREATE;GROUP;IdGrupo`.

## Impact

- New `business.model.Invoice` (with sections and lines), `business.InvoiceBuilder` (abstract), `business.UserInvoiceBuilder`, `business.GroupInvoiceBuilder`, `business.InvoiceDirector`; `ParkController` invoice operations; `CommandProcessor` parses `INVOICE CREATE`.
- No persistence or data-format changes.
