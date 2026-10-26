# Proposal

## Why

Visitors need personal passes on top of their admission: SpeedPasses (priority access, bought singly or in packs of ten) and a Special Pass (access to special attractions). Passes belong to individual tickets, have their own price and consumption status, must be refundable while unused, and must be persisted.

## What Changes

- New commands (listed in `HELP` and `HELP EXTENDED`):
  - `SPEEDPASS CREATE;IdEntrada;Cantidad` — quantity 1 or 10; each SpeedPass is an independent object (generated id `<IdEntrada>-SP<n>`) costing 20% of the ticket's stored price; the response shows the amount charged.
  - `SPECIALPASS CREATE;IdEntrada` — at most one Special Pass per individual ticket (id `<IdEntrada>-SPECIAL`), costing the ticket's stored price.
  - `SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` — refund an unused pass when the ticket's date is strictly after the system date (E2 refund rule); the response shows the refunded amount.
- Passes attach to any individual ticket (also those inside group tickets) that is dated on or after the system date.
- Consumed passes stay in the system with their status (consumption itself is triggered by attraction access in a later change).
- Passes (type, id, ticket, price, consumed status) are persisted in `passes.txt` (also under `--fly`).
- The user invoice gains a "Passes" section with the passes of all the user's individual tickets, including tickets inside group tickets (group invoices are unchanged).
- Interim rule until cascade deletion exists: an individual or group ticket whose individual tickets have passes cannot be deleted.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, consumption status, refunds and persistence.

### Modified Capabilities
- `invoices`: user invoices include the user's personal passes.
- `tickets`: tickets with passes cannot be deleted (until cascade deletion is introduced).
- `uml-model`: the model shows the pass hierarchy.

## Impact

- New model classes `Pass`, `SpeedPass`, `SpecialPass`; `IndividualTicket` holds its passes; new `PassController` (Singleton), `PassRepository` interface and `FilePassRepository`; `TicketController`, `InvoiceDirector`, `CommandProcessor`, `CommandCatalog`, `App`, UML updated.
