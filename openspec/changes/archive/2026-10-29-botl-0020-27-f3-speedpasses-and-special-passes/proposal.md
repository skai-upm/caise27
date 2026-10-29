# Proposal

## Why

Milestone F3-C06 introduces SpeedPasses and Special Passes associated with individual tickets (including tickets inside a group ticket): their purchase, pricing from the associated ticket's stored price, consumption status, refunds, and their removal when the associated ticket is validly deleted.

## What Changes

- Pass model: a common pass abstraction (identifier, price, associated individual ticket, consumed flag) with SpeedPass and Special Pass; consumed passes stay in the system; pass identifiers are unique among passes.
- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (quantity 1 or 10) creates independent SpeedPasses `<purchaseIdentifier>:1..n`, each costing 20 % of the ticket's price, all-or-nothing, and displays the total.
- `SPECIALPASS CREATE;<identifier>;<individualTicket>`: at most one Special Pass per ticket, costing the ticket's price; displays the price.
- Purchases require an existing individual ticket (top-level or part of a group ticket) dated today or later.
- `SPEEDPASS DELETE;<speedPassIdentifier>` / `SPECIALPASS DELETE;<specialPassIdentifier>` refund an unconsumed pass whose ticket date is strictly after the current date.
- Valid TICKET DELETE / GROUPTICKET DELETE also remove all passes of the deleted individual tickets, consistently.
- Passes are persisted (`passes.txt`) through an injected repository; `--fly` covers them.
- HELP / HELP EXTENDED list the four pass commands.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, state, refunds, cascade deletion and persistence.

### Modified Capabilities
- `command-interface`: HELP lists the pass commands.

## Impact

- `business.model.{Pass, SpeedPass, SpecialPass}`, `business.repository.PassRepository` (added to `Repositories`), `persistence.TextFilePassRepository`, `ParkController`, catalog, tests, UML.

## Ambiguities and unmet prerequisites (flagged)

- **Consumption** happens through attraction entry, a later milestone in this batch; here passes carry the consumed state (and refunds honor it) but no command consumes them yet.
- **Invoices:** the user-invoice pass sections belong to F3-C02's requirements, not to this milestone's text; per the agreed scope they are not added here and remain an open gap.
- **"At most one Special Pass throughout its existence":** an existing Special Pass (consumed or not) blocks another purchase; after a valid refund the pass no longer exists in the system, so a new purchase is accepted. The source is silent on that case.
- Pass prices are rounded to cents (half-up), e.g. 20 % of 45.55 = 9.11.
- Output texts: `SPEEDPASS PURCHASED (<n>). TOTAL PRICE: <amount> EUR`, `SPECIALPASS PURCHASED. PRICE: <amount> EUR`, `SPEEDPASS REFUNDED`, `SPECIALPASS REFUNDED`.
