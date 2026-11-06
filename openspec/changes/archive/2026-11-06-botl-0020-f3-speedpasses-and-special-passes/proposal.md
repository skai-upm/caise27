# Proposal

## Why

Milestone F3-C06 adds SpeedPasses and Special Passes: personal extras bought for an individual ticket, including tickets that compose a group ticket. Each pass has its own price, consumption status and refund rules. They must be priced from the stored ticket price, kept as records once consumed, refunded only under strict conditions, and removed together with their ticket when the ticket is validly deleted. The user invoice must also be able to show them (deferred by the invoice milestone until passes existed).

## What Changes

- Add passes, each with its own identifier, price, the individual ticket it belongs to and a consumed flag. There are two kinds: SpeedPass (20% of the ticket's stored price) and Special Pass (100% of it). Pass identifiers form their own unique space, separate from tickets.
- Add `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity 1 or 10. It creates independent passes `<purchaseIdentifier>:1..n` when the ticket exists and is dated today or later, checks every identifier before creating anything, and displays the total charged (no package discount).
- Add `SPECIALPASS CREATE;<identifier>;<individualTicket>`, at most one per individual ticket even if the first one was consumed. Same ticket-date condition, and displays the price.
- Add `SPEEDPASS DELETE;<id>` and `SPECIALPASS DELETE;<id>` (refunds), allowed only for unconsumed passes whose ticket is dated strictly after today. A refunded pass leaves the system.
- `TICKET DELETE` and `GROUPTICKET DELETE`, when valid, also delete every pass of the affected individual tickets. The deletion is all or nothing.
- Persist passes, including their consumed flag.
- User invoices list the user's SpeedPasses and Special Passes (also for tickets in group compositions) and add them to the total. Group invoices never include passes.
- List the four commands in HELP and HELP EXTENDED.

### In scope
- Pass model, purchase, pricing, refund, cascade deletion, persistence, invoice sections, help.

### Out of scope
- Consuming passes at attraction entry (entry and pass consumption milestone). The consumed flag and its effect on refunds are added now.

## Capabilities

### New Capabilities
- `passes`: SpeedPasses and Special Passes linked to individual tickets: purchase, pricing, consumption status and refunds.

### Modified Capabilities
- `tickets`: TICKET DELETE and GROUPTICKET DELETE also delete the passes of the deleted individual tickets.
- `invoices`: the user invoice includes the user's passes.
- `park-persistence`: passes are part of the stored park state.

## Impact

- New `business.model.Pass`, `SpeedPass` and `SpecialPass`. `ParkController` gets pass storage and operations, plus cascades in ticket deletion. `ParkState` gains passes.
- New `persistence.PassRepository` (`passes.txt`), configured in `AppConfiguration` and `StoragePaths`.
- `UserInvoiceBuilder` gets pass steps, and `InvoiceController` supplies the passes. `CommandProcessor` handlers and `CommandCatalog` entries.
- Tests, UML, README.
