# Proposal

## Why

Milestone F3-C06 introduces personal passes linked to individual tickets: SpeedPasses (any number, bought singly or in packs of ten) and at most one Special Pass per ticket. Their purchase, pricing from the ticket's stored price, consumption state, refunds and their removal together with a deleted ticket must be supported, and they must appear on the user's invoice.

## What Changes

- Pass abstraction with identity, price, associated individual ticket and consumed flag; a pass identifier space without duplicates (separate from tickets).
- `SPEEDPASS CREATE;<purchaseId>;<ticket>;<1|10>`: creates `<purchaseId>:1..n` passes at 20% of the ticket price each (no pack discount), all identifiers checked first (no partial pack), for an existing individual ticket (also composition tickets) dated today or later; shows the total.
- `SPECIALPASS CREATE;<id>;<ticket>`: one Special Pass per ticket at the ticket price; a second one is rejected even if the first was consumed; shows the price.
- `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>`: refund only unconsumed passes whose ticket date is strictly after the current date; refunded passes leave the system.
- `TICKET DELETE` and `GROUPTICKET DELETE` also delete the passes of the deleted individual tickets, atomically.
- User invoices list the user's SpeedPasses and Special Passes (also those on composition tickets); group invoices do not add passes.
- Out of scope: consuming passes at attraction entry (F3-C08) and pass persistence (F3-C09); passes live in memory until then.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, state and refunds.

### Modified Capabilities
- `tickets`: ticket and group ticket deletion also removes associated passes.
- `invoices`: user invoices include passes; group invoices exclude them.

## Impact

- New `business.model.Pass`/`SpeedPass`/`SpecialPass`, `persistence.repository.PassRepository`, `business.service.PassService`; `Repositories`/`Park`/initializer; `TicketService` deletion; `UserInvoiceBuilder.passes`; commands and HELP.
