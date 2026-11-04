# Proposal

## Why

Visitors can only buy tickets. Delivery 3 adds personal add-ons attached to individual tickets: SpeedPasses (single or packs of ten) and at most one Special Pass per ticket, each with its own price and consumption status, purchasable, refundable when the ticket date allows it, and kept as records once consumed.

## What Changes

- New abstract `Pass` (identifier, associated `IndividualTicket`, price, consumed status) with `SpeedPass` (20% of the ticket's stored price) and `SpecialPass` (100% of it).
- `SPEEDPASS CREATE;<compra>;<ticket>;<cantidad>`: quantity 1 or 10 only; creates that many independent SpeedPasses identified `<compra>-1 … <compra>-n`; answers `OK: SPEEDPASS CREATED -> <total>` (total = n × single price, no extra discount). All-or-nothing: if any derived identifier already exists, nothing is created.
- `SPECIALPASS CREATE;<id>;<ticket>`: at most one Special Pass per individual ticket, even if consumed; answers `OK: SPECIALPASS CREATED -> <price>`.
- Passes can be bought for any individual ticket — including constituent tickets of group tickets — dated on or after the system date.
- `SPEEDPASS DELETE;<id>` / `SPECIALPASS DELETE;<id>`: refund (remove) an unconsumed pass whose ticket date is strictly after the system date; consumed passes are never removed or refunded.
- **Modified**: constituent tickets of a group ticket now store their discounted share (individual price × (1 − group discount)) as their price, so passes attached to them are priced on that discounted stored price. Group ticket totals are unchanged.
- Deleting a ticket (`TICKET DELETE`, `GROUPTICKET DELETE`, both only for future tickets) also deletes its passes (necessarily unconsumed).
- Passes persist in `passes.txt` and are restored at startup.
- **Modified**: user invoices gain a "Personal passes" section (passes on any of the user's tickets, including constituents) — the step reserved for passes in the invoice builder; group invoices still exclude passes.
- `HELP`/`HELP EXTENDED` list the four commands; UML updated.

## Capabilities

### New Capabilities
- `passes`: Pass data, SpeedPass/Special Pass purchase and pricing, uniqueness, refunds, consumption status, cascade with ticket deletion, persistence.

### Modified Capabilities
- `group-tickets`: "Group ticket composition" — constituent tickets store the discounted per-member price.
- `invoices`: "User invoice contents" and "Builder-based construction" — personal passes section/step.

## Impact

- **Code**: model `Pass`, `SpeedPass`, `SpecialPass`; service `PassService`; persistence `PassRepository`; `TicketService.createGroupTicket` (constituent share); `ParkController` (pass operations, cascades, injected `PassService`); invoice builder step; `CommandProcessor`, `CommandCatalog`; `ApplicationContext` wiring; UML.
- **Data**: new `passes.txt` (`Tipo;Identificador;Ticket;Precio;Consumido`); new constituent lines in `tickets.txt` carry the discounted share.
- **Mandatory milestone message**: `[F3-C06] Add speed and special passes`.
- **Open questions / assumptions**:
  - Single SpeedPass purchases also get the derived identifier `<compra>-1` (uniform rule).
  - Pass identifiers are unique across SpeedPasses and Special Passes (one pass namespace, separate from tickets).
  - Passes cannot be bought for tickets dated before the system date (they would be unusable).
  - A refunded Special Pass purchase is cancelled and does not count towards the one-per-ticket limit; a consumed one always counts.
  - Consumption is only recorded here (status + persistence); when passes are consumed is defined by the attraction-access change.
  - The discounted share is rounded to two decimals, so the sum of shares may differ by cents from the group total, which remains the amount charged for the group ticket.
