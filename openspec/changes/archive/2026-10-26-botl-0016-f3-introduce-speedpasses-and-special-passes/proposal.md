# Proposal

## Why

Visitors want extras attached to their admission: SpeedPasses (sold one by one or in packs of ten) and one Special Pass per ticket. They must be priced from the ticket, persisted, keep their consumption status, be refundable under the E2 date rules, and appear on the user's invoice.

## What Changes

- Commands (ids derived from the ticket id):
  - `SPEEDPASS CREATE;IdTicket;Cantidad` with quantity 1 or 10 → generates that many independent SpeedPasses `<IdTicket>-S<n>`, each priced at 20% of the ticket's stored price; answers `OK SPEEDPASS CREATED -> <importe>` with the ids.
  - `SPECIALPASS CREATE;IdTicket` → one Special Pass `<IdTicket>-SPECIAL` priced as the ticket; at most one per ticket during its whole lifetime (even if used or refunded); answers `OK SPECIALPASS CREATED -> <importe>`.
  - `SPEEDPASS DELETE;IdPase` / `SPECIALPASS DELETE;IdPase` → refund an unused pass when the ticket date is after the system date (same rule as `TICKET DELETE`); the pass is kept with status REFUNDED.
- Passes apply to individual tickets, including the internal tickets of a group ticket; buying requires the ticket date not to be past.
- Each pass stores its id, price and status (AVAILABLE, CONSUMED, REFUNDED). Consumed passes are kept (no command consumes passes yet; status and persistence are ready for attraction access).
- Passes are persisted in `passes.txt` and restored with their ticket; deleting a ticket (or group ticket) deletes its passes.
- User invoices gain a "Personal passes" section (passes not refunded on tickets owned by the user, including internal tickets); group invoices are unchanged.
- HELP catalog lists the new commands; UML updated.
- Required commit title: `[F3-C06] Add speed and special passes`.

## Capabilities

### New Capabilities
- `personal-passes`: SpeedPass and Special Pass purchase, pricing, status, refund and persistence.

### Modified Capabilities
- `invoicing`: user invoices include personal passes.

## Impact

- Model: abstract `Pass`, `SpeedPass`, `SpecialPass`, enum `PassStatus`; `IndividualTicket` composes its passes.
- `TicketManager`/`ParkController`/`CommandProcessor`/`CommandCatalog` gain pass operations; `TicketStorage` gets a third file; `InvoiceBuilder`/`InvoiceDirector` get a personal-passes step.
