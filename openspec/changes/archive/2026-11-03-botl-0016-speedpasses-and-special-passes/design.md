# Design

## Context

Individual tickets (`Ticket`) exist on their own (`TicketController.getTickets()`) or as constituents of a `GroupTicket` (ids `<TG>-<n>`, today stored at the undiscounted unit price). Controllers are singletons wired by `App`; repositories are interfaces. Invoices are built by builders with fixed steps. See proposal.md.

## Goals / Non-Goals

**Goals:** pass purchase/refund rules, prices from the stored ticket price, persistence of passes, invoice completion.

**Non-Goals:** consuming passes at attractions (F3-C08), cascade deletion of passes with tickets and attraction types (F3-C09).

## Decisions

- **Model:** abstract `Pass` (final `id`, `price`, `ticket` reference, `consumed` flag with `markConsumed()`), `SpeedPass` (`RATE = 0.20`) and `SpecialPass` (rate 1.00); constructor computes the price from `ticket.getPrice()` (scale 2, HALF_UP) unless a stored price is given when restoring. `getType()` returns `SPEED`/`SPECIAL` for persistence (named so to avoid clashing with the `TYPE` constants).
- **Ticket lookup:** `TicketController.findIndividualTicket(id)` searches individual tickets and constituent tickets of group tickets.
- **`PassController`** (singleton, injected `PassRepository` + `TicketController`): `buySpeedPasses(id, ticketId, qty, today)` validates qty ∈ {1, 10}, ticket exists and `date >= today` (the brief's acceptance scenario speaks of current or future tickets), derives all ids `<id>-1..n`, checks none exists, then adds them all and saves once (revert on failure) — so no partial pack. `buySpecialPass(id, ticketId, today)` additionally rejects if any existing Special Pass references that ticket (consumed ones included). `refund(type, passId, today)` requires the right type, `!consumed` and `ticket.getDate().isAfter(today)`. Pass ids form one namespace across both types.
- **Refunded Special Pass:** a refund removes the pass, so the ticket may get a Special Pass again; "at most one" counts the passes that exist (consumed ones stay and keep counting). Recorded as an interpretation.
- **Discounted constituent price (MODIFIED group-tickets):** the brief's scenario requires passes on a group member's ticket to use "that ticket's stored discounted price"; constituent tickets therefore store `unit × (100 − discount) / 100`. `GroupTicket` still keeps `unitPrice` and the total; the group ticket file is unchanged (constituent prices are derived on load).
- **Persistence:** `PassFileStorage` (`passes.txt`, `Id;Tipo;IdTicket;Precio;Consumido`), loaded after tickets with a lookup function to individual/constituent tickets; unknown tickets skipped. This is the data contract that F3-C09 will extend with cascades.
- **Known gap until F3-C09:** deleting a future ticket that has passes does not delete them yet; such orphan passes are skipped on the next load.
- **Invoices:** `InvoiceBuilder` gains step `buildPersonalPasses()`; `UserInvoiceBuilder` lists the passes of all its tickets (direct + constituent), `GroupInvoiceBuilder` adds nothing; the director calls the new step after the ticket steps.
- **Responses:** `OK SPEEDPASS CREATED -> 80.00`, `OK SPECIALPASS CREATED -> 40.00`, `OK: Pass S1-1 refunded (8.00)`.

## Risks / Trade-offs

- [Rounding of discounted shares, e.g. 33.33 × 0.9] → each share rounded HALF_UP; the group total keeps its own stored value.
