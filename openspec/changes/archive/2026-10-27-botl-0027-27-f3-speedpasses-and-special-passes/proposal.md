# Proposal

## Why

Milestone F3-C06 adds personal passes attached to individual tickets — SpeedPasses (bought singly or in packs of ten) and at most one Special Pass per ticket — with prices derived from the ticket's own price, a consumption state, refunds of unconsumed future passes, and joint removal when the ticket (or its group ticket) is validly deleted.

## What Changes

- Passes with their own identity (shared pass identifier space), price, associated individual ticket and consumed indicator; consumed passes stay stored as purchase records.
- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (quantity 1 or 10): passes `<purchaseIdentifier>:1..n`, each 20% of the ticket's price; all identifiers checked first; total shown.
- `SPECIALPASS CREATE;<identifier>;<individualTicket>`: one Special Pass per ticket, price equal to the ticket's price; price shown.
- Purchases require an existing individual ticket (also composed ones) dated today or later.
- `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>`: refund of an unconsumed pass whose ticket is dated strictly after today.
- `TICKET DELETE` / `GROUPTICKET DELETE` also remove the passes of the deleted individual tickets, atomically.
- User invoices list the user's SpeedPasses and Special Passes (also those of composed tickets); group invoices do not add them.
- HELP/HELP EXTENDED list the four pass commands.
- Record the mandatory milestone commit `[F3-C06] Add speed and special passes` once verified.

## Capabilities

### New Capabilities
- `pass-management`: SpeedPasses and Special Passes — purchase, pricing, consumption state and refunds.

### Modified Capabilities
- `ticket-management`: deleting tickets also removes their passes.
- `invoicing`: user invoices include the user's passes.
- `command-processing`: HELP lists the pass commands.

## Impact

- New `Pass`, `SpeedPass`, `SpecialPass`, `PassRepository`, `PassService`, `PassOperationException`; `TicketService`, `InvoiceService`, `UserInvoiceBuilder`, `ParkController`, `ParkRepositories`, `CommandProcessor`, `CommandCatalog`; UML. Passes are in memory until F3-C09 extends persistence.
