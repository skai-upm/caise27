# Proposal

## Why

Milestone F3-C06 adds personal add-ons to individual tickets: SpeedPasses (1 or packs of 10) and a Special Pass, with prices derived from the ticket price, a consumption flag, refunds of unconsumed future passes, and cascade deletion together with their ticket.

## What Changes

- Add passes associated with an individual ticket (standalone or part of a group ticket composition), each with identifier, price, associated ticket and consumed flag; consumed passes remain as records. Pass identifiers share one space (not shared with tickets).
- Prices: SpeedPass = 20% of the associated individual ticket's price; Special Pass = 100% of it (discounted price for group composition tickets; later `SYSTEM PRICE` changes do not affect them).
- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (quantity 1 or 10): creates passes `<purchaseIdentifier>:1..n` all-or-nothing after checking every identifier; ticket must exist and be dated today or later; prints the total charged (no pack discount).
- `SPECIALPASS CREATE;<identifier>;<individualTicket>`: at most one Special Pass per ticket; same ticket conditions; prints the price.
- `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>`: refund only unconsumed passes whose ticket date is strictly after the current date.
- `TICKET DELETE` and `GROUPTICKET DELETE` (when allowed) also delete all passes of the affected individual tickets, consistently (validation first, then deletion).
- User invoices now include sections for the user's SpeedPasses and Special Passes (also those on group-ticket composition tickets); group invoices do not include them.
- `HELP`/`HELP EXTENDED` list the four commands.
- Commit with the mandatory message `[F3-C06] Add speed and special passes`.

## Capabilities

### New Capabilities
- `passes`: SpeedPasses and Special Passes — purchase, pricing, identifiers, consumption flag, refunds and cascade deletion.

### Modified Capabilities
- `invoices`: user invoices include pass sections.
- `execution-control`: help lists the pass commands.

## Impact

- New `business.model.Pass` (abstract), `SpeedPass`, `SpecialPass`; `persistence.repository.PassRepository`; `business.service.PassService` (singleton, injected); `ParkController` cascades ticket deletions; invoice builder/director; processor and catalog.
- Ambiguities flagged:
  - Persisting passes belongs to the Delivery 3 persistence milestone (F3-C09); in this milestone passes live in memory during the execution.
  - "At most one Special Pass throughout its existence": a ticket that has a Special Pass (consumed or not) cannot get another; after a refund the pass no longer exists, so a new one may be bought.
  - Consumption is set by attraction access (F3-C08); here passes are created unconsumed.
  - Responses: `SPEEDPASS CREATED. Total price: <amount> EUR`, `SPECIALPASS CREATED. Price: <amount> EUR`, `SPEEDPASS DELETED`, `SPECIALPASS DELETED`.
