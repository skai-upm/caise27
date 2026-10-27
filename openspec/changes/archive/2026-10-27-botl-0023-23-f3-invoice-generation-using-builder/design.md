# Design

## Context

Tickets live in `TicketRepository`; individual tickets know their user and (if composed) their group ticket; group tickets know their group and composition. Passes (SpeedPass, Special Pass) arrive with F3-C06.

## Goals / Non-Goals

**Goals:**
- Builder pattern with a common abstraction; user/group invoices with correct totals; omission of empty sections.

**Non-Goals:**
- Pass sections with data (F3-C06 adds builder steps), invoice persistence.

## Decisions

- **Product:** `Invoice` (immutable: `title`, `List<String> header`, `List<InvoiceSection> sections`, `BigDecimal total`, `render()`), `InvoiceSection` (title + lines), `InvoiceLine` (description, amount, detail lines, `countsInTotal`).
- **Builder:** `abstract class InvoiceBuilder` with common steps `addHeaderLine`, `addLine(sectionTitle, line)` (creates the section on first use, so empty sections never exist), `build()` (computes the total from lines that count). `UserInvoiceBuilder` adds `forUser(User)` and `addIndividualTicket(IndividualTicket)`; `GroupInvoiceBuilder` adds `forGroup(Group)` and `addGroupTicket(GroupTicket)` (composition as detail lines, not counted). Each builder exposes only the steps meaningful for its recipient — no null-filled common signatures.
- **Director:** `InvoiceService.createUserInvoice(email)` / `createGroupInvoice(id)` traverses the tickets (users and groups do not store tickets), drives the steps and returns `builder.build()`. Unknown user/group → `InvoiceOperationException` (extends `ParkException`).
- **Rendering:**
  ```
  INVOICE - USER
  Name: Ana Garcia Lopez
  DNI: 12345678Z
  Email: ana@correo.com
  Individual tickets:
    T001 (2027-04-15): 40.00 EUR
  TOTAL: 40.00 EUR
  ```
  Group detail lines are indented under their group ticket.
- **Package:** `es.upm.amusementpark.business.invoice`; UML updated accordingly.
- **Mandatory commit:** `[F3-C02] Add invoice generation with builder` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Pass sections cannot be demonstrated yet] → The section mechanism is generic; F3-C06 adds the steps and tests.
