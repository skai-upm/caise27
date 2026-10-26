# Design

## Context

`TicketController` holds all individual tickets (standalone and internal) and group tickets; an internal ticket's owner is found by searching group tickets. Personal passes do not exist yet (introduced in F3-C06). `CommandProcessor` formats multi-line responses (HELP, ATTRACTION STATUS).

## Goals / Non-Goals

**Goals:**
- A formal Builder: product, builder abstraction, concrete builder, and a director deciding the steps per recipient type.
- Leave a clear extension point for pass sections without inventing passes now.

**Non-Goals:**
- Persisting invoices, invoice numbering, taxes.

## Decisions

- **Product** (`business.invoice`): `Invoice` (recipient description, `List<InvoiceSection>`, total) — immutable, created only by the builder; `InvoiceSection` (title, `List<InvoiceLine>`); `InvoiceLine` (description, amount) as records.
- **Builder abstraction**: interface `InvoiceBuilder` with steps `recipient(String)`, `section(String title)`, `item(String description, BigDecimal amount)` (adds to the current section) and `build()`; each step returns the builder. **Concrete builder** `StandardInvoiceBuilder` accumulates the parts, drops empty sections, sums the total with `BigDecimal` and resets after `build()`.
- **Director**: `InvoiceController` (business controller) receives `UserController`, `GroupController`, `TicketController` and a `Supplier<InvoiceBuilder>`. `forUser(email)`: recipient → section "Individual tickets" with the user's standalone tickets (`TicketController.getStandaloneTicketsOf(user)`, which excludes internal tickets) → build. `forGroup(groupId)`: recipient → section "Group tickets" with the group's group tickets (`getGroupTicketsOf(group)`) → build. Sections are only started when there are items. Passes will add a further user section in F3-C06.
- **Command**: `INVOICE CREATE;<USER|GROUP>;<id>`; the type is matched case-insensitively; other values are rejected. Formatting in `CommandProcessor`: `OK INVOICE <TYPE> <id>`, `Recipient: …`, then per section `<title>:` and `- <description> <amount>` lines, then `Total: <amount>`.
- **UML**: new package `business.invoice` with the product classes and the builder interface/implementation (`StandardInvoiceBuilder ..|> InvoiceBuilder`), `Invoice *--> InvoiceSection`, `InvoiceSection *--> InvoiceLine`, `InvoiceController` associations.

## Risks / Trade-offs

- [Invoices list historical purchases, including past tickets] → Intended: an invoice is a statement of all purchases on record.
