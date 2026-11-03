# Design

## Context

Purchases live in `TicketController`: individual tickets (`Ticket`, owner reference) and group tickets (`GroupTicket`, group reference, composition of constituent tickets that are not in the individual list). `ParkController` holds the system date. Passes do not exist yet. See proposal.md.

## Goals / Non-Goals

**Goals:** a real Builder with a common abstraction and distinct steps; recipient-specific content without double counting; no persistence.

**Non-Goals:** passes (F3-C06), invoice numbering, persistence, taxes.

## Decisions

- **Abstraction:** interface `InvoiceBuilder` in `business.invoice` with steps `buildHeader(LocalDate issueDate)`, `buildIndividualTickets()`, `buildGroupTickets()` and `getResult()`. Each concrete builder implements every step; steps that do not apply to its recipient add nothing. Alternative — one class with optional constructor parameters — is exactly what the source forbids.
- **Concrete builders:** `UserInvoiceBuilder(User, List<Ticket> directTickets)` adds the "Individual tickets" section; `GroupInvoiceBuilder(Group, List<GroupTicket>)` adds the "Group tickets" section (line text includes members and discount). A step only adds a section when it has at least one line.
- **Director:** `InvoiceDirector.construct(InvoiceBuilder, LocalDate)` calls the steps in a fixed order and returns `getResult()`, so construction order is defined once.
- **Product:** `Invoice` (recipient text, issue date, `List<InvoiceSection>`, `getTotal()` = sum of all lines), `InvoiceSection` (title, `List<InvoiceLine>`, subtotal), `InvoiceLine` (description, amount) in `business.model`; all immutable.
- **Data selection** in `ParkController`: user invoice uses `tickets.getTickets()` filtered by `belongsTo(user)` (only directly bought tickets; constituent tickets are not in that list, so they cannot be counted); group invoice uses `tickets.getGroupTickets()` filtered by `belongsTo(group)`. Historical tickets are included because they were purchased. Prices are the captured `getPrice()` values, never recomputed.
- **Rendering** (presentation) — multi-line text:
  ```
  INVOICE USER ana@correo.com (Ana Garcia Lopez) - 2026-10-01
  Individual tickets:
    T001 2026-10-15 40.00
  Total: 40.00
  ```
- **Forward compatibility for passes (recorded, not implemented):** when personal passes exist (F3-C06), add a step `buildPersonalPasses()` to `InvoiceBuilder`: the user builder will list the user's passes, including those attached to constituent tickets, while the group builder will add nothing, so members' passes never enter the group total.

## Risks / Trade-offs

- [Invoice for a deleted user] → impossible: only registered users are recipients; their historical tickets are still listed.
