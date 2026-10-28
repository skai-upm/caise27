# Design

## Context

Layered packages exist (`presentation`, `business`, `persistence`). Tickets are reachable through `TicketService.findAll()` and `IndividualTicket.isPartOfGroupTicket()`; group tickets know their group. Passes arrive in F3-C06. See proposal.md.

## Goals / Non-Goals

**Goals:** explicit Builder with differentiated steps; conditional sections; extensible for passes.
**Non-Goals:** persisting invoices; pass sections (F3-C06).

## Decisions

- **Product** `business.invoice.Invoice`: `kind` (USER/GROUP), `headerLines`, ordered `sections` (`InvoiceSection(title, items)`), `total`. `InvoiceItem` is either *charged* (description + amount, counted in the total) or *detail* (description only, e.g. composition lines) — created via two factory methods, so no nullable amount leaks into signatures.
- **Builder abstraction** `InvoiceBuilder` (abstract class): common steps `startSection(title)`, `charge(description, amount)`, `detail(description)`, `header(line)`, and `build()` which drops empty sections and sums charged items. Concrete **`UserInvoiceBuilder`** adds `user(User)` and `directTicket(IndividualTicket)`; **`GroupInvoiceBuilder`** adds `group(Group)` and `groupTicket(GroupTicket)` (charge total + composition details). F3-C06 will add pass steps to `UserInvoiceBuilder`.
- **Director** `business.service.InvoiceService`: `userInvoice(email)` looks up the active user, traverses all tickets choosing standalone individual tickets of that user, and calls the steps; `groupInvoice(groupId)` likewise for group tickets with that group.
- **Presentation** `InvoiceFormatter` renders the product: title line, header lines, each section title followed by indented items (`<desc>: <amount> EUR` or `<desc>`), then `TOTAL: <amount> EUR`.

## Risks / Trade-offs

- [Two concrete builders share little] → common behavior (sections, totals, omission) lives in the abstract builder.
