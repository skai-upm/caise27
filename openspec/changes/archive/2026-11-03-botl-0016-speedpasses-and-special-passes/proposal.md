# Proposal

## Why

Individual tickets need optional personal add-ons: SpeedPasses (bought singly or in packs of ten) and one Special Pass per ticket, each with its own price, consumption status, persistence and refund rules.

## What Changes

- New abstract `Pass` (identifier, price, associated individual ticket, consumed flag) with `SpeedPass` and `SpecialPass`; passes attach to individual tickets, including the constituent tickets of group tickets (e.g. `TG01-2`).
- `SPEEDPASS CREATE;<IdCompra>;<IdTicket>;<Cantidad>` with quantity 1 or 10 creates that many independent SpeedPasses `<IdCompra>-1 … <IdCompra>-n`, each priced at 20% of the ticket's stored price; responds `OK SPEEDPASS CREATED -> <total>`. All identifiers are checked first: no partial packs.
- `SPECIALPASS CREATE;<IdPase>;<IdTicket>` creates the ticket's only Special Pass, priced at 100% of the ticket's stored price; responds `OK SPECIALPASS CREATED -> <precio>`. Rejected if the ticket already has one, consumed or not.
- `SPEEDPASS DELETE;<IdPase>` / `SPECIALPASS DELETE;<IdPase>` refund an unconsumed pass whose ticket date is strictly after the system date. Consumed passes stay recorded and are never refundable.
- Passes persisted in `passes.txt` (id, type, ticket, price, consumed) and restored at startup.
- Constituent tickets of a group ticket now store the member's discounted share (individual price minus the group discount), so passes on them are based on that discounted price.
- User invoices gain a "Personal passes" section (passes on the user's tickets, constituent ones included); group invoices do not include members' passes.
- No pass consumption at attractions yet (F3-C08); no cascade deletion yet (F3-C09).

## Capabilities

### New Capabilities
- `personal-passes`: SpeedPass and Special Pass purchase, prices, data and refunds.
- `pass-file-persistence`: storage and reconstruction of passes.

### Modified Capabilities
- `group-tickets`: constituent tickets are priced at the discounted share.
- `invoices`: user invoices list personal passes.
- `application-control`: HELP lists the four pass commands.
- `domain-model-diagram`: diagram shows the pass hierarchy.

## Impact

- New model classes, `PassController` (singleton), `PassRepository`/`PassFileStorage`; `TicketController.findIndividualTicket`; `ParkController`, `CommandProcessor`, `CommandCatalog`, invoice builders, `App`; runtime file `passes.txt` (git-ignored); `uml/model.puml`.
