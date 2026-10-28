# Design

## Context

Layers: `presentation`, `business` (`controller`, `model`, `exception`), `persistence`. `ParkController` can traverse tickets (`getTickets`, `findGroupTickets`, `findTicketsOfUser`). Ticket prices: `IndividualTicket.getUnitPrice()`, `Ticket.getPrice()` (sum of composition for group tickets).

## Goals / Non-Goals

**Goals:** Builder-based invoices for users and groups; commands; text rendering.

**Non-Goals:** persisting invoices; pass sections (F3-C06 adds the step's content).

## Decisions

- **Product** (`business.invoice`): immutable `Invoice` (title, `List<String>` recipient lines, `List<InvoiceSection>`, total `BigDecimal`); `InvoiceSection` (title, `List<InvoiceItem>`); `InvoiceItem` (description, amount, `List<String>` details). No public constructor with optional parameters: only builders create invoices.
- **Builder abstraction**: `abstract class InvoiceBuilder` with `reset()`, abstract `buildRecipient()`, abstract `buildSections()`, final `getResult()`; protected helpers `addRecipientLine`, `addSection(title, items)` that **omits empty sections**, and total computed from the items actually shown. Concrete `UserInvoiceBuilder(User, List<IndividualTicket> directTickets)` with steps `buildRecipient()` and `buildSections()` → `buildIndividualTicketSection()` (+ `buildPassSections()` hook, empty until F3-C06); `GroupInvoiceBuilder(Group, List<GroupTicket>)` → `buildGroupTicketSection()` with composition as item details.
- **Director** `InvoiceDirector.construct(InvoiceBuilder)`: `reset → buildRecipient → buildSections`, returning `getResult()`. Avoids per-variant manual assembly.
- **Controller**: `createUserInvoice(email)` (active user via `findUser`, direct tickets = top-level `IndividualTicket`s of that user) and `createGroupInvoice(id)` (existing group, `findGroupTickets`). Not-found → `NotFoundException`.
- **Presentation**: `InvoiceCreateCommand` (`INVOICE CREATE;<USER|GROUP>;<id>`) and `InvoiceFormatter` rendering:
  ```
  INVOICE <title>
  <recipient lines>
  [<section title>]
    <description> ... <amount>
      - <detail>
  TOTAL: <total>
  ```
