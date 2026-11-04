# Design

## Context

- `TicketManager` holds standalone `IndividualTicket`s, whose prices are captured at purchase, and `GroupTicket`s, which store their total and constituents.
- `ParkController` (business.service) is the entry point for presentation.
- Passes do not exist yet.

See proposal.md.

## Goals / Non-Goals

**Goals:**
- A textbook Builder (abstract builder, concrete builders, director, product), with sections omitted when empty.

**Non-Goals:**
- Persisting invoices, tax or invoice numbering, passes (deferred to the change that introduces them).

## Decisions

- **Product (business.model):**
  - `InvoiceLine(description, amount)`.
  - `InvoiceSection(title, lines)`, with `subtotal()`.
  - `Invoice(recipient, sections)`, with `total()` as the sum of the subtotals.
  - All are records with unmodifiable lists, so they are immutable and have no optional-parameter constructors.
- **Builder (business.service):**
  - abstract `InvoiceBuilder` keeps the recipient text and the section list, with `protected addSection(title, lines)` that ignores empty line lists;
  - abstract steps `buildHeader()`, `buildIndividualTickets()`, `buildGroupTickets()`, plus a concrete `getResult()`;
  - `UserInvoiceBuilder(User, Collection<IndividualTicket>)`: `buildIndividualTickets` adds the standalone tickets where `isOwnedBy(user)`; `buildGroupTickets` adds nothing, since group tickets are not charged to members;
  - `GroupInvoiceBuilder(Group, Collection<GroupTicket>)`: `buildIndividualTickets` adds nothing; `buildGroupTickets` adds the tickets where `belongsTo(group)`.
  - `InvoiceDirector.construct(builder)` calls the three steps in order and returns `getResult()`.
- **No double counting by construction:** the user builder receives only `TicketManager.getTickets()`, which excludes constituents, so constituent tickets can never appear on a user invoice.
- **Prices:** captured prices (`IndividualTicket.getPrice()`) and stored group totals (`GroupTicket.getPrice()`) are used, never recomputed.
- **Lines:** `"<id> <date>"` for tickets. Group-ticket lines add `"(<n> members, <d>% discount)"` from the stored total, the constituent count and `discountPercent`.
- **`ParkController.createUserInvoice(email)` / `createGroupInvoice(groupId)`** validate the recipient and run the director. `CommandProcessor` parses `INVOICE CREATE;USER|GROUP;<id>` and formats:
  ```
  OK: invoice for user Ana Garcia Lopez <ana@correo.com>
    Individual tickets:
      T001 2026-10-15: 40.00
      subtotal: 40.00
    TOTAL: 40.00
  ```
- **Future passes:** the pass change will add a `buildPasses()` step to the abstraction and the director. Personal passes attached to constituent tickets will then appear on user invoices but not on group invoices, as the source states.

## Risks / Trade-offs

- [Historical group tickets of a deleted group cannot be invoiced] → Only existing groups can be invoiced, per the source.
