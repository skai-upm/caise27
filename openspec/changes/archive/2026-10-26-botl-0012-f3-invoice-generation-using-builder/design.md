# Design

## Context

Layers: `presentation` (`CommandProcessor`), `business.controller`, `business.model`, `persistence`. Tickets: `IndividualTicket` (standalone or internal to a `GroupTicket`), `GroupTicket` (group, composed tickets, discount, total). `TicketController.findOwner` tells whether an individual ticket is internal. See proposal.md.

## Goals / Non-Goals

**Goals:**
- GoF Builder: product, builder interface, concrete builder, director.
- Invoices built in explicit steps; recipient-specific content chosen by the director.

**Non-Goals:**
- Persisting invoices; passes (added in C06).

## Decisions

- **Product** `Invoice` (immutable): `title`, `recipient`, `List<InvoiceSection>`, `total`; `InvoiceSection` (`title`, `List<InvoiceLine>`); `InvoiceLine` (`description`, `amount`). `Invoice.toText()` renders the lines.
- **Builder interface** `InvoiceBuilder`: `startInvoice(title, recipient)`, `startSection(title)`, `addLine(description, amount)`, `build()`. `startInvoice` resets state so one builder instance can produce many invoices.
- **Concrete builder** `TextInvoiceBuilder`: collects sections, ignores sections left empty, accumulates the total from line amounts, and builds the `Invoice`. Alternative: telescoping `Invoice` constructors — rejected by the epic.
- **Director** `InvoiceController` (business.controller): `userInvoice(email)` and `groupInvoice(groupId)` decide which steps apply: user → section "Individual tickets" with standalone `IndividualTicket`s of the user; group → section "Group tickets" with that group's `GroupTicket`s (`<id> <date> <n> members -<d>%`). The builder is received in the constructor.
- **Output**: `OK INVOICE`, `Invoice: <title>`, `Recipient: <recipient>`, sections (`<title>:` then `  - <description>: <amount>`), or `No billable items`, then `TOTAL: <amount>`.
- **UML**: invoice package, `InvoiceBuilder` interface realized by `TextInvoiceBuilder`, `Invoice *--> InvoiceSection *--> InvoiceLine`, `InvoiceController x--> InvoiceBuilder : builder`.

## Risks / Trade-offs

- [Tickets of a deleted group are not reachable by group id] → group invoices are only for existing groups, matching the command.
