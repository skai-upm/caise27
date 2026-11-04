# Proposal

## Why

Individual tickets have no optional add-ons. Delivery 3 introduces personal passes attached to individual tickets: SpeedPasses, bought singly or in packs of ten, and at most one Special Pass per ticket. Each pass has its own price and consumption state, can be refunded under the E2 date rules, and is kept after use.

## What Changes

- New model: abstract `Pass` (id, price, associated `IndividualTicket`, consumed) with `SpeedPass` and `SpecialPass`.
- New commands:
  - `SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad`: quantity 1 or 10, each pass at 20% of the ticket price; replies `OK SPEEDPASS CREATED -> <total>`.
  - `SPECIALPASS CREATE;IdPase;IdTicket`: one per ticket for its lifetime, at 100% of the ticket price; replies `OK SPECIALPASS CREATED -> <price>`.
  - `SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase`: refund only unconsumed passes whose ticket date is after the system date.
- Passes attach to standalone or constituent individual tickets. All identifiers and rules are checked before anything is created, so a purchase is never partial.
- Passes persist in `passes.txt` with their consumed state. Consumption itself (attraction entry with passes) belongs to a later change.
- Invoices: user invoices now add a passes section covering passes on all tickets the user owns, including constituents; group invoices exclude members' passes. This completes the step deferred by the invoice change.
- `HELP`, the command catalog and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `passes`: Pass data, SpeedPass and Special Pass purchase, refund, consumption state and persistence.

### Modified Capabilities
- `invoices`: "User invoice" includes personal passes; "Group invoice" explicitly excludes members' passes.
- `application-control`: "HELP command" lists the four pass commands.

## Impact

- New: `Pass`, `SpeedPass`, `SpecialPass` (model); `PassManager` (singleton service); `PassFileStorage`; tests.
- Changed: `TicketManager` (`findIndividualTicket` incl. constituents), `ParkController`, `ParkSession` (`passes.txt`), `DataFiles`, `InvoiceBuilder` (new `buildPasses` step) and builders, `CommandProcessor`, `CommandCatalog`, `uml/model.puml`.
