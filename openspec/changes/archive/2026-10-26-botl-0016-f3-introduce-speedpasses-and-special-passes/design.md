# Design

## Context

Tickets: abstract `Ticket` ← `IndividualTicket` (price, owner, groupTicketId) / `GroupTicket` (components). `TicketManager` (singleton) persists through `TicketRepository` (`TicketStorage`: tickets.txt, group_tickets.txt). Invoices via Builder with steps. E2 refund/deletion rule: ticket date strictly after the system date. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Passes as part of their individual ticket (composition), uniform status handling via a small hierarchy.

**Non-Goals:**
- Consuming passes at attractions (next deliveries), discounts on packs.

## Decisions

- **Model**: abstract `Pass` (`id`, `price`, `status : PassStatus`; `consume()`, `refund()` — both only from AVAILABLE); `SpeedPass` and `SpecialPass` extend it; `PassStatus` enum `AVAILABLE, CONSUMED, REFUNDED`. `IndividualTicket` composes `List<SpeedPass> speedPasses` and `SpecialPass specialPass` (0..1) and offers `addSpeedPasses(int)` (ids continue `S<n>` from the list size), `addSpecialPass()` (fails if one exists in any status), `getPasses()`. Prices: `price × 0.20` HALF_UP to 2 decimals; special = `price`. A refunded pass is kept with status REFUNDED (history, lifetime rule for Special Pass) — refunding is the business meaning of the `DELETE` commands.
- **Manager**: `TicketManager.buySpeedPasses(ticketId, qty, today)`, `buySpecialPass(ticketId, today)`, `refundPass(passId, Class<? extends Pass> type, today)`; lookups over standalone + internal individual tickets; persist after each change.
- **Persistence**: `TicketStorage(ticketsFile, groupTicketsFile, passesFile)` adds `passes.txt` lines `SPEED|SPECIAL;IdPase;IdTicket;Precio;Estado`, restored after tickets by ticket id (unknown ticket → skipped). Ticket deletion removes the ticket object and therefore its passes from the next save.
- **Invoices**: new builder step `buildPersonalPasses()` (no-op by default) implemented by `UserInvoiceBuilder`; `InvoiceDirector` calls it after the group-tickets step. Line text `SpeedPass T001-S1 (ticket T001)`.
- **Presentation**: four `CommandCatalog` entries; responses `OK SPEEDPASS CREATED -> 80.00 (T001-S1..T001-S10)`, `OK SPECIALPASS CREATED -> 40.00 (T001-SPECIAL)`, `OK: SpeedPass T001-S1 refunded -> 8.00`.

## Risks / Trade-offs

- [Pass ids derived from ticket ids] → unique because ticket ids are unique and suffixes are fixed.
- [Old data has no passes.txt] → missing file means no passes.
