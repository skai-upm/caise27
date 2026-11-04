# Design

## Context

Layers: `presentation` (`CommandProcessor`, `CommandRunner`), `business.controller.ParkController`, `business.model`, `business.service`, `persistence`. `TicketService` holds individual tickets (including group-ticket constituents) and group tickets; `GroupTicket.contains(IndividualTicket)` identifies constituents. Passes are not implemented yet.

## Goals / Non-Goals

**Goals:** a textbook Builder (abstraction + concrete builders + director), no double counting, omitted empty sections.

**Non-Goals:** passes (later change), persistence of invoices, taxes/discount breakdowns beyond the stored prices.

## Decisions

- **Product** (`business.model`): `Invoice(String recipient, List<InvoiceSection> sections, BigDecimal total)`, `InvoiceSection(String title, List<InvoiceLine> lines)` with `getSubtotal()`, `InvoiceLine(String description, BigDecimal amount)` — small immutable objects; no optional-parameter constructors.
- **Builder** (`business.service`): `abstract class InvoiceBuilder` holding the parts under construction, with steps `buildRecipient()`, `buildIndividualTicketsSection()`, `buildGroupTicketsSection()` (default: no-op, i.e. the section does not apply) and a final `getInvoice()` that computes the total from the sections. A protected helper `addSection(title, lines)` ignores empty line lists, so empty sections are omitted in one place. Abstract class instead of interface so that the shared assembly code and the default "not applicable" steps are not duplicated.
  - `UserInvoiceBuilder(User, List<IndividualTicket> directTickets)` overrides recipient and individual tickets.
  - `GroupInvoiceBuilder(Group, List<GroupTicket> groupTickets)` overrides recipient and group tickets.
- **Director**: `InvoiceDirector.construct(InvoiceBuilder)` calls the steps in a fixed order and returns `getInvoice()`. Adding the passes step later means one new step in the abstraction and director.
- **Data selection**: `TicketService.directTicketsOf(User)` = individual tickets owned by that user that are not constituents of any group ticket; `groupTicketsOf(Group)` = group tickets whose group is that object. The controller resolves the recipient (`UserService.findByEmail`, `GroupService.find`), chooses the builder, and calls the director.
- **Presentation**: `CommandProcessor` parses `INVOICE CREATE;USER|GROUP;id` and renders:
  ```
  OK: Invoice for user Ana Garcia Lopez <ana@correo.com>
    Individual tickets:
      T001 (2026-10-15): 40.00
    Total: 40.00
  ```
  and for groups `Invoice for group G01 - Familia Garcia`, section `Group tickets:` with lines `TG01 (2026-10-15, 3 members): 108.00`.

## Risks / Trade-offs

- [Historic tickets of a deleted group are not invoiceable] → the recipient must exist; acceptable.
