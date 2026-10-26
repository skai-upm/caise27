# Design

## Context

Purchases live in `TicketController` (`IndividualTicket`, `GroupTicket` composed of internal `IndividualTicket`s). Users and groups are found through `UserController`/`GroupController`. Layers: presentation → business (model, controller, validation) → persistence.

## Goals / Non-Goals

**Goals:** formal Builder (builder interface + concrete builder + director + product); correct exclusion of internal tickets.

**Non-Goals:** persistence of invoices, taxes, the passes section (no passes exist yet; added with F3-C06).

## Decisions

- **Package `es.upm.parque.business.invoice`**:
  - `Invoice` (product, immutable): `recipientType`, `recipientId`, `recipientName`, `issueDate`, `List<InvoiceSection> sections`, `total`; `format()` renders text.
  - `InvoiceSection` (`title`, `List<InvoiceLine> lines`, `subtotal`) and `InvoiceLine` (`description`, `amount`).
  - `InvoiceBuilder` interface — steps `reset()`, `buildHeader(type, id, name, date)`, `buildSection(title, lines)`, `getResult()`.
  - `DefaultInvoiceBuilder` — accumulates header and sections; `getResult()` computes the total and returns the `Invoice`.
  - `InvoiceDirector` — `constructUserInvoice(builder, user, directTickets, date)` and `constructGroupInvoice(builder, group, groupTickets, date)`; it calls `buildSection` only for non-empty data, so only applicable sections appear.
  Alternative: a fluent static builder inside `Invoice` — rejected because the director/step structure makes the "distinct steps per recipient" explicit and lets a later change add the passes step in one place.
- **`InvoiceController`** (business.controller) receives `UserController`, `GroupController`, `TicketController`, `SystemDate`, an `InvoiceBuilder` and an `InvoiceDirector`; it gathers the data and asks the director to construct the invoice. Issue date = system date.
- **Direct tickets**: `TicketController.directTicketsOf(User)` returns the user's `IndividualTicket`s not contained in any `GroupTicket`; `groupTicketsOf(Group)` returns the group's `GroupTicket`s.
- **Output**: `OK INVOICE CREATED` followed by the formatted invoice, e.g.
  ```
  INVOICE USER 12345678Z - Ana Garcia Lopez
  Date: 2026-10-01
  Individual tickets:
    T001 2026-10-15 ........ 40.00
  TOTAL: 40.00
  ```

## Risks / Trade-offs

- [Passes are part of the user-invoice requirement in the original request] → not implementable until passes exist; the director has a single place to add that section.
