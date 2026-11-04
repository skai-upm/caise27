# Design

## Context

- `IndividualTicket` stores its captured price.
- Constituent tickets live inside `GroupTicket`s (ids `<id>-<k>`).
- Managers are singletons wired by `ParkSession`.
- The invoice builder has the steps header / individual tickets / group tickets.

See proposal.md.

## Goals / Non-Goals

**Goals:**
- Passes as first-class objects tied to individual tickets, with a single source of truth for ticket price and pass identity.

**Non-Goals:**
- Using passes at attractions (F3-C08), cascade deletion and orphan prevention (F3-C09), refund of consumed passes.

## Decisions

- **Model:** abstract `Pass(id, IndividualTicket ticket, String ticketId, BigDecimal price, boolean consumed)` with `markConsumed()`, and subclasses `SpeedPass` and `SpecialPass`. `ticketId` keeps the link for persistence and history (like `ownerDni`). Prices are computed once at purchase (`ticket price × 0.20` HALF_UP to 2 decimals, or `× 1`) and stored, never recomputed.
- **`PassManager` (singleton):**
  - ordered map by id;
  - `buySpeedPasses(id, ticket, quantity)` validates quantity ∈ {1, 10}, builds all ids, checks every one is unused, then saves once;
  - `buySpecialPass(id, ticket)` checks no Special Pass references that ticket;
  - `refund(id, kind, today)` requires the right kind, not consumed and `ticket.date > today`;
  - `passesFor(ticket)`.
  - It is injected with `PassFileStorage` and `TicketManager` (to resolve ticket ids on load).
- **Ticket lookup:** `TicketManager.findIndividualTicket(id)` returns a standalone ticket or a constituent of a group ticket; group-ticket ids are not individual tickets.
- **Identifiers:** a pack of 10 gets `IdCompra-1..10`; quantity 1 uses `IdCompra` unchanged, so users refer to a single pass by the id they chose.
- **Special Pass after refund:** a refunded Special Pass no longer exists, so a new one can be bought. The "lifetime" rule is enforced against existing (consumed or unconsumed) passes. Open question recorded: whether a refund should also block repurchase.
- **Purchase date:** the source sets no date restriction on buying passes, so none is added (open question).
- **Persistence:** `passes.txt`, format `SPEED|SPECIAL;id;ticketId;price;consumed`. Unknown ticket ids load with `ticket = null` (history), and such passes cannot be refunded.
- **Invoices:** new abstract step `buildPasses()` run by `InvoiceDirector` after the ticket steps.
  - `UserInvoiceBuilder` receives the passes and the user's owned tickets (standalone + constituents) and lists passes whose ticket is owned by the user.
  - `GroupInvoiceBuilder.buildPasses()` adds nothing.
- **Replies:** `OK SPEEDPASS CREATED -> <total>` and `OK SPECIALPASS CREATED -> <price>`, mirroring the ticket literals.

## Risks / Trade-offs

- [Deleting a ticket that has passes leaves orphan passes until F3-C09] → Documented. F3-C09 defines deletion/cascade strategy.
