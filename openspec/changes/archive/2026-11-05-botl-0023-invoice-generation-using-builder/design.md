# Design

## Context

Tickets are traversed from `TicketRepository` (users/groups hold no ticket references). Passes arrive in F3-C06 and must be able to appear on user invoices.

## Goals / Non-Goals

**Goals:** Builder with shared steps and recipient-specific steps; no double counting. **Non-Goals:** persisting invoices, pass lines (later step).

## Decisions

- **Package `business.invoice`**:
  - `InvoiceLine` (record: description, amount, counted) — `counted=false` for descriptive detail such as group ticket composition.
  - `Invoice` (immutable: type title, recipient lines, ordered sections `Map<String, List<InvoiceLine>>`, total) with `render()`.
  - `abstract class InvoiceBuilder<B extends InvoiceBuilder<B>>`: common steps `recipient(String... lines)`, protected `section(title, lines)` (ignores empty sections), `build()` (sums counted lines). Self-typed for fluent chaining.
  - `UserInvoiceBuilder` (`forUser(User)`, `directTickets(List<IndividualTicket>)`; F3-C06 adds `passes(...)`), `GroupInvoiceBuilder` (`forGroup(Group)`, `groupTickets(List<GroupTicket>)`).
- **Director**: `business.service.InvoiceService` selects the data (direct tickets = the user's individual tickets with no containing group ticket; group tickets referencing the group) and drives the builder steps.
- **Command**: one `INVOICE CREATE` entry with 3 elements, dispatching on `USER`/`GROUP` (other values → invalid data). HELP shows both forms.
- **Scope**: all purchases (active and historical) are invoiced, since invoices reflect economic information.

## Risks / Trade-offs

- [Rounding] → prices already have two decimals; sums are exact with `BigDecimal`.
