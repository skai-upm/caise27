# Design

## Context

Layered app with singleton controllers configured by `ApplicationContext`; `TicketManager` holds standalone `IndividualTicket`s and `GroupTicket`s (constituents reachable via `getTickets()`, ids `<TG>-<DNI>`, stored price = undiscounted individual price). Invoices are built with `InvoiceBuilder` steps. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** pass hierarchy, purchase/refund rules, persistence, invoice pass section.

**Non-Goals:** consuming passes at attractions (change "Attraction access using passes"); cascade deletion of passes when tickets are deleted (change "Extended persistence"). Until then, deleting a future ticket leaves its passes referencing it; after a restart those pass lines become history (preserved, ids reserved).

## Decisions

- **Model** (`business.model`): abstract `Pass` (`id`, `price`, `ticket : IndividualTicket`, `consumed`, `consume()`), `SpeedPass`, `SpecialPass`; static `SpeedPass.priceFor(ticket)` = 20% (`HALF_UP`, 2 decimals), `SpecialPass.priceFor(ticket)` = ticket price.
- **`PassManager`** singleton (`configure(PassRepository, TicketManager)`), operations `buySpeedPasses(purchaseId, ticketId, quantityText, today)`, `buySpecialPass(id, ticketId, today)`, `refund(id, kind, today)`, `passesOf(User)`, `find(id)`; save-then-swap atomicity; ids unique across both kinds and historical lines.
- **Ticket lookup**: `TicketManager.findIndividualTicket(id)` searches standalone tickets and group-ticket constituents.
- **Persistence**: `PassRepository`/`PassFileStore`, line `SPEED|SPECIAL;Id;IdTicket;Precio;consumed`; load resolves tickets through `findIndividualTicket`; unresolved/malformed lines kept as history.
- **Rules**: quantity text must be exactly `1` or `10`; purchase requires ticket date ≥ system date (brief: "valid current or future ticket"); Special Pass uniqueness checked against all stored passes of that ticket regardless of consumption; refund needs unconsumed and ticket date > system date (E2 future rule).
- **Refunded Special Pass**: the refund removes the purchase, so a new Special Pass may be bought for that ticket afterwards (the lifetime rule targets consumed/kept passes). Recorded as interpretation.
- **Output**: `OK: SpeedPasses SP01-1..SP01-10 purchased for ticket T001. Total: 80.00`, `OK: Special Pass SPC1 purchased for ticket T001. Total: 40.00`, `OK: Pass SP01-3 refunded: 8.00`.
- **Invoice**: `InvoiceBuilder.buildPasses(List<Pass>)` (no-op default), overridden by `UserInvoiceBuilder` (section "Personal passes"); director calls it after the ticket steps; `ParkController` passes the user's passes.
- **Wiring**: `ApplicationContext` creates `PassFileStore(passes.txt)` and configures `PassManager` after `TicketManager`; `ParkController.configure` receives it.

## Risks / Trade-offs

- [Brief S6 expects discounted constituent price] → stored (undiscounted) price used per authoritative source; flagged in proposal.
