# Proposal

## Why

Milestone F3-C06 adds personal passes attached to individual tickets (including tickets inside a group ticket): SpeedPasses (20% of the ticket's price, bought singly or in packs of ten) and Special Passes (same price as the ticket, at most one per ticket), with purchase, pricing, a consumption state, refunds, and cascading removal when the ticket or group ticket is validly deleted.

## What Changes

- Add a pass abstraction (identifier, price, associated individual ticket, consumed flag) with SpeedPass and Special Pass types; passes have their own duplicate-free identifier space (not shared with tickets) and remain stored when consumed.
- Prices are computed from the price stored in the associated individual ticket (discounted price for tickets in a group ticket): SpeedPass 20%, Special Pass 100%.
- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (quantity 1 or 10, identifiers `<purchaseIdentifier>:<n>`, all identifiers checked first, total shown).
- `SPECIALPASS CREATE;<identifier>;<individualTicket>` (at most one per ticket, consumed passes still count, price shown).
- `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>`: refund only unconsumed passes whose ticket date is strictly after the current date; the refunded pass leaves the persisted state.
- `TICKET DELETE` and `GROUPTICKET DELETE` also remove the passes of the deleted individual tickets, all-or-nothing.
- Persist passes; show the user's passes (on direct and group-composed tickets) on user invoices; group invoices exclude passes.
- Extend HELP/HELP EXTENDED and the UML model.
- Commit with the exact message `[F3-C06] Add speed and special passes`.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, consumption state and refunds.

### Modified Capabilities
- `tickets`: TICKET DELETE also removes the ticket's passes.
- `group-tickets`: GROUPTICKET DELETE also removes the passes of its composition.
- `invoices`: user invoices include SpeedPass and Special Pass sections.
- `park-persistence`: passes are part of the persisted state.
- `execution-control`: HELP lists the four pass commands.

## Impact

- New model classes, pass registry and operations in `ParkController`, pass repository port/adapter (`passes.txt`), four commands, invoice builder step, catalogue entries, UML, tests.
- **Flagged ambiguities:**
  - Consuming passes is milestone F3-C08; this change stores and exposes the consumed state (and a model operation to consume), but no command consumes passes yet.
  - "At most one Special Pass throughout its entire existence": an existing pass (consumed or not) blocks a second purchase; a refunded pass is removed from the system, so a new purchase after a refund is allowed.
  - Pass prices are rounded to cents (HALF_UP).
  - Output texts: `SPEEDPASS CREATED: <n> pass(es). TOTAL PRICE: <total>`, `SPECIALPASS CREATED. PRICE: <price>`, `SPEEDPASS DELETED`, `SPECIALPASS DELETED`.
