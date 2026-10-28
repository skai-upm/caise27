# Design

## Context

Individual tickets (standalone or in group compositions) are found through `TicketRepository.findIndividualById`; controllers are injected singletons wired by `ParkInitializer`; invoices are built by `UserInvoiceBuilder` driven by `InvoiceService`. See proposal.md.

## Goals / Non-Goals

**Goals:** pass hierarchy reusing common behavior; atomic purchases; consistent cascades.
**Non-Goals:** consumption on entry (F3-C08); persistence of passes (F3-C09).

## Decisions

- **`model.Pass` (abstract)**: `id`, `IndividualTicket ticket`, `consumed`; `abstract BigDecimal getPrice()`, `consume()`, `isRefundableOn(today)` = `!consumed && ticket.isDeletableOn(today)`. `SpeedPass` price = ticket price × 0.20 (HALF_UP, cents); `SpecialPass` price = ticket price. Pass → ticket is unidirectional (tickets do not know passes).
- **`persistence.repository.PassRepository`**: ordered map by id; `findByTicket`, `delete`.
- **`business.service.PassService`** (singleton, `configure(PassRepository, TicketService, Supplier<LocalDate>)`): `buySpeedPasses` checks quantity, ticket, date, then all ids, then saves; `buySpecialPass`; `refundSpeedPass` / `refundSpecialPass` (type-checked); `passesOf(ticket)`, `deletePassesOf(tickets)`.
- **Cascade** in `ParkController.deleteIndividualTicket` / `deleteGroupTicket`: the ticket service validates and deletes (throws before changing anything), then the pass service removes the passes of the deleted ticket's individual tickets.
- **Invoices**: `UserInvoiceBuilder.speedPass(...)` / `specialPass(...)` steps; `InvoiceService` adds passes whose ticket's user is the invoiced user.

## Risks / Trade-offs

- [Passes are lost on restart until F3-C09] → explicitly scoped; documented in the proposal.
