# Proposal

## Why

Milestone F3-C06 adds personal extras bought for individual tickets — SpeedPasses (single or packs of ten) and at most one Special Pass per ticket — with prices derived from the ticket's stored price, a consumption state, refunds, and joint refund when their ticket is deleted.

## What Changes

- Add passes with their own identity: identifier, price, associated individual ticket (direct or part of a group ticket) and a consumed flag; consumed passes stay in the system. One identifier space for all passes (not shared with tickets).
- Prices: SpeedPass = 20% of the associated ticket's stored price; Special Pass = 100% of it (group discounts included; later SYSTEM PRICE changes do not affect them).
- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity 1 or 10, creating passes `<purchaseIdentifier>:1..n` atomically; shows the total charged.
- `SPECIALPASS CREATE;<identifier>;<individualTicket>`: at most one Special Pass per ticket; shows the price.
- Purchases require an existing individual ticket dated on or after the current date.
- `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>`: refund only unconsumed passes whose ticket is dated strictly after the current date; refunded passes leave the system.
- TICKET DELETE and GROUPTICKET DELETE also delete all passes of the deleted individual tickets.
- User invoices gain SpeedPass and Special Pass sections for passes of the user's individual tickets (including tickets inside group tickets); group invoices never add passes.
- HELP lists the four commands.
- Commit with the exact message `[F3-C06] Add speed and special passes`.

## Capabilities

### New Capabilities
- `passes`: SpeedPasses and Special Passes — purchase, pricing, consumption state, refunds and cascade deletion.

### Modified Capabilities
- `invoices`: user invoices include the user's passes.

## Impact

- New `business.model.{Pass,SpeedPass,SpecialPass}`, `persistence.repository.PassRepository`, `business.controller.PassController`, `presentation.PassCommands`; `TicketRepository.findIndividualTicket`; `TicketController` cascades; `InvoiceController` and `ApplicationInitializer` wiring; help catalogue.
- Passes are kept in memory in this milestone; their persistence is F3-C09 ("Persistence must be extended to retain the new elements and states"). Consumption happens on attraction entry (F3-C08); here only the state and its effect on refunds exist.
- Ambiguities flagged: a refunded Special Pass no longer exists, so a new one may be bought for that ticket (only consumption is said to block a second purchase); responses `SPEEDPASS CREATED - Total price: <x> EUR`, `SPECIALPASS CREATED - Price: <x> EUR`, `SPEEDPASS DELETED`, `SPECIALPASS DELETED`; pass prices rounded half-up to cents.
