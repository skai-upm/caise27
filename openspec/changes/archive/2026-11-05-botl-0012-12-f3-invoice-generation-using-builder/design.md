# Design

## Context

Layered packages (presentation → business → persistence → business.model). `TicketManager` exposes standalone individual tickets (`getTickets()`, each with `getUser()`) and group tickets (`getGroupTickets()`, each with `getGroup()`, `getTickets()`, `getPrice()`); constituent tickets are only reachable through their group ticket, so they are naturally separate from direct purchases. `ParkController` coordinates operations. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- GoF Builder: abstract builder with step methods, concrete builders per recipient, director fixing the order, immutable product.

**Non-Goals:**
- Persisting invoices, invoice numbering, taxes, passes (not implemented yet).

## Decisions

- **Product** `business.model.Invoice`: recipient label, issue date (system date), ordered `List<Section>` (title + `List<Line>` of description and amount) and total; nested records `Section`, `Line`; immutable; `format()` returns the printable text. Builders accumulate sections themselves and create the `Invoice` (three required arguments, no optional ones) in `getResult()`.
- **Builder abstraction** `business.InvoiceBuilder` (abstract class): `reset(recipient, date)`, step methods `buildIndividualTickets(List<IndividualTicket>)` and `buildGroupTickets(List<GroupTicket>)` with default no-op bodies, protected `addSection(title, lines)` that ignores empty sections, and `getResult()` computing the total from the included lines. An abstract class (rather than an interface) lets the common section/total logic live once.
- **Concrete builders**: `UserInvoiceBuilder` overrides `buildIndividualTickets` (section "Individual tickets"); `GroupInvoiceBuilder` overrides `buildGroupTickets` (section "Group tickets", line shows ticket count and discount). The step not applicable to a recipient keeps the no-op default.
- **Director** `business.InvoiceDirector.construct(builder, recipient, date, directTickets, groupTickets)` calls `reset`, `buildIndividualTickets`, `buildGroupTickets` in that order, then `getResult()`.
- **Data selection** in `ParkController`: user invoice → `tickets.getTickets()` filtered by `getUser() == user` (constituents are never in this list, so they cannot be counted twice); group invoice → `tickets.getGroupTickets()` filtered by `getGroup() == group`. All purchases still linked in memory are included regardless of date (an invoice is an accounting view, not a validity check).
- **Output**:
  ```
  OK: INVOICE USER ana@correo.com - Ana Garcia Lopez
  Date: 2026-10-01
  Individual tickets:
    T001 2026-10-15 40.00
  Total: 40.00
  ```
- **Passes**: when passes are added later, a `buildPasses(...)` step with a no-op default is added to `InvoiceBuilder` and overridden by `UserInvoiceBuilder`; existing steps stay unchanged.

## Risks / Trade-offs

- [Historical tickets of deleted users are only kept as raw lines] → they have no user to invoice; such users cannot be addressed by email anyway.
