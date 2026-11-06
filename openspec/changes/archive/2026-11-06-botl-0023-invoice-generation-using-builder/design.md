# Design

## Context

See proposal.md – Why. Tickets: abstract `Ticket` (`getPrice()`), `IndividualTicket` (user), `GroupTicket` (group, composition); `TicketRepository.findAll()` holds top-level tickets only (constituents live inside group tickets). Users and groups hold no ticket references, so invoices traverse tickets.

## Goals / Non-Goals

**Goals:**
- Builder pattern with a director; sections only when they have content.

**Non-Goals:**
- Formatting beyond legible text; invoice numbering or persistence.

## Decisions

- **Package `es.upm.park.business.invoice`**:
  - `InvoiceLine` (record: description, `BigDecimal` amount or none for detail lines), `InvoiceSection` (title + lines), `Invoice` (immutable: type, recipient lines, sections, total; `render()` to text).
  - `InvoiceBuilder` (interface — the common abstraction): `reset(type)`, `recipient(String line)`, `section(String title)`, `item(String description, BigDecimal amount)`, `detail(String text)`, `build()`.
  - `TextInvoiceBuilder implements InvoiceBuilder`: accumulates steps; `build()` drops sections without lines and computes the total from priced items. One concrete builder suffices because both invoice types share the same product; the variants differ in the steps the director calls.
- **Director `InvoiceService`** (business.service): `userInvoice(email)` — recipient lines; "Individual tickets" section with top-level `IndividualTicket`s whose user is that user (by DNI); a "Passes" step reserved for F3-C06 (called only when passes exist). `groupInvoice(id)` — recipient lines; "Group tickets" section with each `GroupTicket` whose `getGroup() == group`, an item for its total and `detail` lines for each constituent (`id`, user email, price) — details carry no amount, so they never enter the total.
- **Command**: `INVOICE CREATE` with 2 fields; `USER`/`GROUP` dispatch; other → `Error: unknown invoice type X`.
- **Rendering**: `INVOICE (USER)` / `INVOICE (GROUP)`, recipient lines, each section title followed by indented lines `<description>  <amount>`, details indented further, and `TOTAL: <amount>`.
