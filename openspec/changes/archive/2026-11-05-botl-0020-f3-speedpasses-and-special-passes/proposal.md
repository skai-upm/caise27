# Proposal

## Why

Milestone F3-C06 adds personal SpeedPasses and Special Passes, bought for an individual ticket (including group-ticket components). It covers their pricing from the ticket's stored price, a consumed state, refunds, and cascading deletion when their ticket is deleted. They are also personal purchases that user invoices must show.

## What Changes

- Add a pass model: an abstract `Pass` with `SpeedPass` and `SpecialPass`, each with an identifier, price, associated individual ticket and consumed flag, in their own identifier space.
- Add `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (1 or 10, ids `<purchase>:<n>`, atomic, total displayed) and `SPECIALPASS CREATE;<identifier>;<individualTicket>` (at most one per ticket, price displayed).
- Add the refunds `SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>`: only unconsumed passes whose ticket is dated strictly in the future.
- `TICKET DELETE` and `GROUPTICKET DELETE` also delete the associated passes (**MODIFIED** `tickets`).
- User invoices list the user's passes (**MODIFIED** `invoices`).
- Passes are persisted, including the consumed flag (**MODIFIED** `park-persistence`).
- `HELP` and `HELP EXTENDED` list the four pass commands.

### Assumptions / deferred
- Consumption itself (using a pass when entering an attraction) belongs to the next access milestone. This change adds the consumed state and a business operation to mark it.
- "At most one Special Pass throughout its existence" is enforced against the passes held in the system. A refunded Special Pass leaves the system (as the source states for refunds), so it doesn't block a later purchase.

## Capabilities

### New Capabilities
- `passes`: SpeedPasses and Special Passes: identity, pricing, purchase, consumed state and refunds.

### Modified Capabilities
- `tickets`: ticket and group-ticket deletion cascade to the associated passes.
- `invoices`: user invoices include personal passes.
- `park-persistence`: passes are persisted.

## Impact

- New `Pass`, `SpeedPass`, `SpecialPass`, `presentation.PassCommands` and `persistence.PassRepository`.
- `ParkController` (pass registry, cascades, invoice director).
- `ParkStorage`, `CommandCatalog`, UML and tests.
