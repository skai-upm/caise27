# Design

## Context

Layered code (botl-0011). Tickets: standalone `IndividualTicket` (with `getGroupTicketId() == null`), `GroupTicket` with internal `IndividualTicket`s; `TicketManager.getTickets()` returns top-level tickets. `ParkController` owns the system date. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Textbook Builder (Builder + ConcreteBuilders + Director + Product) where recipient variants differ only in which steps do work.

**Non-Goals:**
- Persisting invoices, taxes, personal passes (not implemented yet), numbering invoices.

## Decisions

- **Product (business.model)**: `Invoice(recipient, issueDate, List<InvoiceSection>)` immutable with `getTotal()`; `InvoiceSection(title, List<InvoiceLine>)` with `getSubtotal()`; `InvoiceLine(description, amount)`. Three required parameters at most — no telescoping/optional constructor arguments.
- **Builder (business.controller)**: abstract `InvoiceBuilder` keeps the parts under construction (`issueDate`, `recipient`, `sections`) and defines steps `reset(LocalDate)`, `buildHeader()` (abstract), `buildIndividualTickets()`, `buildGroupTickets()` (default: not applicable, no-op) and `getResult()`. Protected helper `addSection(title, lines)` ignores empty sections → "only available data".
  - `UserInvoiceBuilder(User, Collection<Ticket>)`: header = user name/email/DNI; individual-ticket step lists top-level `IndividualTicket`s owned by the user (internal tickets are never top-level, so they are excluded by construction).
  - `GroupInvoiceBuilder(Group, Collection<Ticket>)`: header = group id/name; group-ticket step lists `GroupTicket`s bought for the group with members count and discount.
- **Director**: `InvoiceDirector.construct(InvoiceBuilder, LocalDate)` runs reset → header → individual tickets → group tickets → result. A future "personal passes" step is added once to the abstract builder and director.
- **Controller**: `ParkController.createUserInvoice(email)` / `createGroupInvoice(groupId)` resolve the recipient, pick the builder and call the director with the system date.
- **Presentation format**:
  ```
  INVOICE - User Ana Garcia Lopez <ana@correo.com> DNI 12345678Z
    Date: 2026-10-15
    Individual tickets
      Ticket T001 for 2026-10-15: 40.00
      Subtotal: 40.00
    TOTAL: 40.00
  ```

## Risks / Trade-offs

- [Tickets whose owner/group was deleted] → they no longer match any existing recipient; invoices are only for existing users/groups.
