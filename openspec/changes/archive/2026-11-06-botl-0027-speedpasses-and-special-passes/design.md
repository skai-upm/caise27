# Design

## Context

See proposal.md – Why. Tickets: `IndividualTicket` (top-level or inside `GroupTicket.composition`), prices fixed at purchase. Controllers are singletons configured by `ParkController.start(ParkRepositories, today)`. `InvoiceService` is the invoice director.

## Goals / Non-Goals

**Goals:**
- Pass price behavior defined once; consistent cascades.

**Non-Goals:**
- Consumption trigger (F3-C08) and persistence (F3-C09); passes live in memory in this step.

## Decisions

- **`Pass` (abstract, business.model)**: final `id`, `IndividualTicket ticket`, `BigDecimal price` computed in the constructor as `ticket.getPrice() × priceFactor()` (HALF_UP, 2 decimals), `boolean consumed` with `consume()`; `isRefundable(today)` = `!consumed && ticket.getDate().isAfter(today)`. `SpeedPass` (factor 0.20) and `SpecialPass` (factor 1).
- **`PassRepository`** (persistence.repository, in-memory, part of `ParkRepositories`): `existsId`, `add(all)`, `remove`, `findById`, `findByTicket`, `findAll`.
- **`PassService`** (singleton, `configure(PassRepository, TicketService, Supplier<LocalDate>)`): `buySpeedPasses`, `buySpecialPass`, `refundSpeedPass`, `refundSpecialPass`, `removeAllFor(List<IndividualTicket>)`. Purchase checks: ticket via `TicketService.findIndividual(id)` (top-level or constituent), date ≥ today, quantity ∈ {1,10}, all ids free, one Special Pass per ticket among existing passes.
- **Assumption**: "at most one Special Pass throughout its existence" is enforced against existing passes; a refunded Special Pass is no longer part of the system, so it does not block a new one.
- **Cascades in `ParkController`**: `deleteTicket(id)` and `deleteGroupTicket(id)` first delete the ticket through `TicketService` (which validates and persists, failing without changes), then remove the passes of the affected individual tickets (in memory, cannot fail).
- **Invoices**: `InvoiceService` receives the `PassRepository`; user invoices add "SpeedPasses" and "Special passes" sections for passes whose ticket `involves(user)`; group invoices are unchanged.
- **Messages**: `SpeedPasses created: SP02:1..SP02:10 total 80.00`, `Special pass created: PE01 price 40.00`, `SpeedPass refunded: SP02:3`, `Special pass refunded: PE01`.
