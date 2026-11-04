# Design

## Context

Layers: model (`IndividualTicket` with stored price; `GroupTicket` with constituents), services (`TicketService`), controller (`ParkController` singleton with injected services), presentation (`CommandProcessor`, `CommandCatalog`), composition root `ApplicationContext`. Constituent tickets currently store the undiscounted unit price. Invoices: `InvoiceBuilder` steps recipient/individual/group.

## Goals / Non-Goals

**Goals:** pass hierarchy, all-or-nothing purchases, refunds by date, persistence, invoice passes step.

**Non-Goals:** consuming passes at attractions (attraction-access change), extended persistence formats (later change).

## Decisions

- **Model**: `abstract class Pass { id; IndividualTicket ticket; BigDecimal price; boolean consumed; consume(); abstract String getKind(); }`; `SpeedPass` (`PRICE_RATE = 0.20`), `SpecialPass` (`1.00`); static factories `SpeedPass.forTicket(id, ticket)` compute the price from `ticket.getPrice()`; a constructor taking the stored price/consumed is used when loading.
- **Service** `PassService(PassRepository, TicketService)`: `buySpeedPasses(compra, ticketId, quantityText, today)`, `buySpecialPass(id, ticketId, today)`, `refund(id, kindClass, today)`, `passesOf(IndividualTicket)`, `removePassesOf(Collection<IndividualTicket>)`, `find`. Validation before mutation; one save per operation (atomic like other services).
- **Ticket lookup**: individual tickets only (`TicketService.find`), so constituents are included and group ticket ids are rejected.
- **Constituent share**: `TicketService.createGroupTicket` stores `GroupTicket.shareFor(unitPrice, members)` = `unit × (100 − d)/100` (2 decimals) in each constituent; `GroupTicket.unitPrice` and `total` unchanged.
- **Cascades** in `ParkController`: `deleteTicket(id)` and `deleteGroupTicket(id)` capture the affected individual tickets, delete them via `TicketService`, then `passes.removePassesOf(...)`.
- **Persistence**: `PassRepository` (`passes.txt`), loaded after tickets with a `ticketById` resolver; unknown tickets skipped with a warning.
- **Invoices**: new builder step `buildPassesSection()` (default no-op) called by the director after the group tickets step; `UserInvoiceBuilder` receives the user's passes (`PassService.passesOfUser(User)` over all the user's individual tickets, constituents included). `GroupInvoiceBuilder` does not override it.
- **Wiring**: `ParkController.initialize(users, groups, tickets, attractions, passes, invoiceDirector[, date])`; `ApplicationContext` creates `PassRepository`/`PassService`.
- **Presentation**: commands and responses as in the spec; catalog entries with descriptions.

## Risks / Trade-offs

- [Rounding of shares] → documented; the group ticket total remains the charged amount.
- [Larger controller initializer] → acceptable; parameters are the injected collaborators.
